package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import nu.xom.ParsingException;

import org.apache.commons.io.IOUtils;
import org.dom4j.DocumentException;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

public class RSpecValidationTest {

	final static int trueAds = 0;
	final static int ads = 1;
	final static int trueManifests = 2;
	final static int manifests = 3;
	final static int trueRequests = 4;
	final static int requests = 5;
	final static int different = 6;
	final static int typeMissing = 7;
	final static double nano = 1000000000.0;

	@Test
	public void testGetType() {
		List<Entry<String, String>> types = new ArrayList<>();

		String pathRequest = "./src/test/resources/geni/request/request_unbound.xml";
		Entry<String, String> pair1 = new java.util.AbstractMap.SimpleEntry<>(
				AbstractConverter.RSPEC_REQUEST, pathRequest);
		types.add(pair1);

		String pathAd = "./src/test/resources/geni/advertisement/advertisement_paper2015.xml";
		Entry<String, String> pair2 = new java.util.AbstractMap.SimpleEntry<>(
				AbstractConverter.RSPEC_ADVERTISEMENT, pathAd);
		types.add(pair2);

		String pathManifest = "./src/test/resources/geni/manifest/manifest_paper2015.xml";
		Entry<String, String> pair3 = new java.util.AbstractMap.SimpleEntry<>(
				AbstractConverter.RSPEC_MANIFEST, pathManifest);
		types.add(pair3);

		String pathTosca = "./src/test/resources/tosca/request-dummy.xml";
		Entry<String, String> pair4 = new java.util.AbstractMap.SimpleEntry<>(
				AbstractConverter.TOSCA, pathTosca);
		types.add(pair4);

//		String pathTtl = "./src/test/resources/omn/request.ttl";
//		Entry<String, String> pair5 = new java.util.AbstractMap.SimpleEntry<>(
//				AbstractConverter.TTL, pathTtl);
//		types.add(pair5);

		for (int i = 0; i < types.size(); i++) {

			System.out.println("***********************");
			System.out.println("***** test " + i + " ******");
			System.out.println("***********************");
			String path = types.get(i).getValue();
			String type = types.get(i).getKey();
			System.out.println(path + "  " + type);

			String rspecString = null;
			String typeTest = null;
			try {
				rspecString = AbstractConverter.toString(path.substring(20));
				typeTest = RSpecValidation.getType(rspecString);
				Assert.assertTrue("request type matches for pair " + i,
						typeTest.equals(type));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static ArrayList<List<Double>> getErrorDirectory(File path) {

		if (!path.isDirectory()) {
			System.out.println("Not a directory.");
			return null;
		}
		System.out
				.println("==========================================================");
		try {
			System.out.println("Testing all RSpecs in folder:");
			System.out.println("canon path " + path.getCanonicalPath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		File[] files = null;
		if (path.listFiles() != null) {
			files = path.listFiles();
		}

		System.out
				.println("==========================================================");

		List<Double> errorAds = new ArrayList<Double>();
		List<Double> errorManifests = new ArrayList<Double>();
		List<Double> errorRequests = new ArrayList<Double>();
		ArrayList<List<Double>> errorRates = new ArrayList<List<Double>>();
		errorRates.add(errorAds);
		errorRates.add(errorManifests);
		errorRates.add(errorRequests);

		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()
					&& RSpecValidation.rspecFileExtension(files[i])) {
				String rspecString = null;
				boolean valid = false;
				try {
					System.out.println(files[i].getPath().substring(20));
					rspecString = AbstractConverter.toString(files[i].getPath()
							.substring(20));
					valid = RSpecValidation.validateRspecXMLUnit(rspecString);
					System.out.println("Valid: " + valid);
				} catch (IOException e) {
					e.printStackTrace();
				}

				System.out.println(files[i].getPath().substring(20));
				System.out.println(files[i].getPath());
				if (valid) {

					// note: substring(20) specifically gets rid of
					// "./src/test/resources"), so must be changed if a
					// different path is used

					String type = RSpecValidation.getType(rspecString);
					double errorRate = RSpecValidation
							.getProportionalError(rspecString);
					System.out.println("Error: " + errorRate);

					switch (type) {
					case "advertisement":
						errorAds.add(errorRate);
						break;
					case "manifest":
						errorManifests.add(errorRate);
						break;
					case "request":
						errorRequests.add(errorRate);
						break;
					}
				}
			} else if (files[i].isDirectory()) {
				ArrayList<List<Double>> newErrorRates = getErrorDirectory(files[i]);
				for (int j = 0; j < errorRates.size(); j++) {
					errorRates.get(j).addAll(newErrorRates.get(j));
				}
			}
			System.out
					.println("==========================================================");
		}

		double sum = 0;
		for (int i = 0; i < errorRates.size(); i++) {
			double interimSum = 0;
			for (int j = 0; j < errorRates.get(i).size(); j++) {
				interimSum += errorRates.get(i).get(j);
			}

			String type = null;
			switch (i) {
			case 0:
				type = "advertisement";
				break;
			case 1:
				type = "manifest";
				break;
			case 2:
				type = "request";
				break;
			}
			System.out.println("Total error for " + errorRates.get(i).size()
					+ " " + type + "s " + interimSum);
			sum += interimSum;
		}

		int numFiles = errorAds.size() + errorManifests.size()
				+ errorRequests.size();
		System.out.println("Total error for " + numFiles + " files: " + sum);
		System.out.println("Comprising " + errorAds.size() + " ads, "
				+ errorManifests.size() + " manifests, and "
				+ errorRequests.size() + " requests.");
		double average = sum / numFiles;
		System.out.println("Average error: " + average);

		return errorRates;
	}

	private static ArrayList<List<Long>> getTimesDirectory(File path) {
		if (!path.isDirectory()) {
			System.out.println("Not a directory.");
			return null;
		}
		System.out
				.println("==========================================================");
		try {
			System.out.println("Testing all RSpecs in folder:");
			System.out.println("canon path " + path.getCanonicalPath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		File[] files = null;
		if (path.listFiles() != null) {
			files = path.listFiles();
		}

		System.out.println();
		System.out
				.println("==========================================================");

		List<Long> timeAds = new ArrayList<Long>();
		List<Long> timeManifests = new ArrayList<Long>();
		List<Long> timeRequests = new ArrayList<Long>();
		ArrayList<List<Long>> times = new ArrayList<List<Long>>();
		times.add(timeAds);
		times.add(timeManifests);
		times.add(timeRequests);

		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()
					&& RSpecValidation.rspecFileExtension(files[i])) {
				String rspecString = null;
				try {
					System.out.println(files[i].getPath());
					System.out.println(files[i].getPath().substring(20));

					rspecString = AbstractConverter.toString(files[i].getPath()
							.substring(20));
					boolean validXMLUnit = RSpecValidation
							.validateRspecXMLUnit(rspecString);
					System.out.println("validXMLUnit: " + validXMLUnit);
					boolean validRSpecLint = RSpecValidation
							.rspecLintMacOnly(files[i].getPath().substring(20));

					if (validXMLUnit && validRSpecLint) {
						// note: substring(20) specifically gets rid of
						// "./src/test/resources"), so must be changed if a
						// different path is used

						String type = RSpecValidation.getType(rspecString);
						long time = System.nanoTime();
						RSpecValidation.completeRoundtrip(rspecString);
						time = System.nanoTime() - time;
						System.out.println("Time to complete round trip: "
								+ time);

						if (type != null) {
							switch (type) {
							case "advertisement":
								timeAds.add(time);
								break;
							case "manifest":
								timeManifests.add(time);
								break;
							case "request":
								timeRequests.add(time);
								break;
							}
						}
					}
					System.out
							.println("==========================================================");

				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (files[i].isDirectory()) {
				ArrayList<List<Long>> newTimes = getTimesDirectory(files[i]);
				for (int j = 0; j < times.size(); j++) {
					times.get(j).addAll(newTimes.get(j));
				}
			}
		}

		long sum = 0;
		for (int i = 0; i < times.size(); i++) {
			double interimSum = 0;
			for (int j = 0; j < times.get(i).size(); j++) {
				interimSum += times.get(i).get(j);
			}

			String type = null;
			switch (i) {
			case 0:
				type = "advertisement";
				break;
			case 1:
				type = "manifest";
				break;
			case 2:
				type = "request";
				break;
			}
			double seconds = interimSum / nano;
			System.out.println("Total time for " + times.get(i).size() + " "
					+ type + "s " + seconds);
			double average = seconds / times.get(i).size();
			System.out.println("Average time for " + times.get(i).size() + " "
					+ type + "s " + average);
			sum += interimSum;
		}

		int numFiles = timeAds.size() + timeManifests.size()
				+ timeRequests.size();
		System.out.println("Total time for " + numFiles + " files: " + sum);
		System.out.println("Comprising " + timeAds.size() + " ads, "
				+ timeManifests.size() + " manifests, and "
				+ timeRequests.size() + " requests.");
		long average = sum / numFiles;
		double seconds = average / nano;
		System.out.println("Average time: " + seconds + " seconds");

		return times;
	}

	private static int[] validateDirectory(File path) {
		System.out
		.println("==========================================================");
		System.out
		.println("===========  validateDirectory test           ============");

		if (!path.isDirectory()) {
			System.out.println("Not a directory.");
			return null;
		}
		System.out
				.println("==========================================================");
		try {
			System.out.println("Testing all RSpecs in folder:");
			System.out.println("canon path " + path.getCanonicalPath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		File[] files = null;
		if (path.listFiles() != null) {
			files = path.listFiles();
		}

		System.out.println();
		System.out
				.println("==========================================================");

		int[] valid = new int[8];

		for (int i = 0; i < files.length; i++) {

			if (files[i].isFile()
					&& RSpecValidation.rspecFileExtension(files[i])) {
				String rspecString = null;
				try {
					// System.out.println(files[i].getPath());
					System.out.println(files[i].getPath().substring(20));
					rspecString = AbstractConverter.toString(files[i].getPath()
							.substring(20));
					String type = RSpecValidation.getType(rspecString);

					// String schemaPath = null;
					// if (type.equals("request")) {
					// schemaPath =
					// "./src/main/resources/geni/request/request.xsd";
					// } else if (type.equals("advertisement")) {
					// schemaPath =
					// "./src/main/resources/geni/advertisement/ad.xsd";
					// } else if (type.equals("manifest")) {
					// schemaPath =
					// "./src/main/resources/geni/manifest/manifest.xsd";
					// }
					// boolean validSax;
					// try {
					// validSax = RSpecValidation.validateSAX(
					// files[i].getPath(), schemaPath);
					// System.out.println("validSax: " + validSax);
					// } catch (ParserConfigurationException | SAXException e) {
					// e.printStackTrace();
					// }
					//
					// boolean validDom;
					// try {
					// validDom = RSpecValidation.validateDOM(
					// files[i].getPath(), schemaPath);
					// System.out.println("validDom: " + validDom);
					// } catch (ParserConfigurationException | SAXException e) {
					// e.printStackTrace();
					// }
					//
					// boolean validDom4j;
					// try {
					// validDom4j = RSpecValidation.validateDom4j(
					// files[i].getPath(), schemaPath);
					// System.out.println("validDom4j: " + validDom4j);
					// } catch (ParserConfigurationException | SAXException
					// | DocumentException e) {
					// e.printStackTrace();
					// }
					//
					// boolean validXom;
					// try {
					// validXom = RSpecValidation.validateXom(
					// files[i].getPath(), schemaPath);
					// System.out.println("validXom: " + validXom);
					// } catch (ParserConfigurationException | SAXException
					// | DocumentException | ParsingException e) {
					// e.printStackTrace();
					// }
					// boolean validSchemaFactory = RSpecValidation
					// .validateRspecSchemaFactory(files[i].getPath(),
					// type);
					// System.out.println("validSchemaFactory: "
					// + validSchemaFactory);
					
					 boolean validXMLUnit = RSpecValidation
					 .validateRspecXMLUnit(rspecString);
					 System.out.println("validXMLUnit: " + validXMLUnit);

					boolean validRSpecLint = RSpecValidation
							.rspecLintMacOnly(files[i].getPath().substring(20));
					System.out.println("validRSpecLint: " + validRSpecLint);

					switch (type) {
					case "advertisement":
						if (validXMLUnit && validRSpecLint) {
						// if (validRSpecLint) {
							valid[trueAds]++;
						}
						valid[ads]++;
						break;
					case "manifest":
						if (validXMLUnit && validRSpecLint) {
						// if (validRSpecLint) {
							valid[trueManifests]++;
						}
						valid[manifests]++;
						break;
					case "request":
						// if (validXMLUnit && validRSpecLint) {
						if (validRSpecLint) {
							valid[trueRequests]++;
						}
						valid[requests]++;
						break;
					}

					 if ((validXMLUnit || validRSpecLint)
					 && !(validXMLUnit && validRSpecLint)) {
					 valid[different]++;
					 }

					System.out
							.println("==========================================================");
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (files[i].isDirectory()) {
				int[] newValid = validateDirectory(files[i]);
				for (int j = 0; j < valid.length; j++) {
					valid[j] = newValid[j] + valid[j];
				}
			}
		}
		System.out.println("Summary: ");
		int total = valid[ads] + valid[manifests] + valid[requests]
				+ valid[typeMissing];
		int totalTrue = valid[trueAds] + valid[trueManifests]
				+ valid[trueRequests];
		System.out.println(totalTrue + " / " + total + " RSpecs, "
				+ " were valid in both XMLUnit and RSpecLint.");
		System.out.println(valid[trueAds] + " / " + valid[ads]
				+ " ads were valid");
		System.out.println(valid[trueManifests] + " / " + valid[manifests]
				+ " manifests were valid");
		System.out.println(valid[trueRequests] + " / " + valid[requests]
				+ " requests were valid");
		System.out.println("Number where XMLUnit and RSpecLint disagreed: "
				+ valid[different]);

		return valid;

	}

	private static void getErrorFile(File path) {

		System.out.println("******************************************");
		System.out.println("******       Prelim                 ******");
		System.out.println("******************************************");

		DateFormat dateFormat = new SimpleDateFormat("dd MMMMM yyyy");
		Date date = new Date();

		System.out.println("Date of test: " + dateFormat.format(date));
		System.out.println(path.getPath().substring(20) + "\n\n");

		String rspecString = null;
		try {
			rspecString = AbstractConverter.toString(path.getPath().substring(
					20));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			if (rspecString == null) {
				System.out
						.println("Something went wrong trying to get the input file. Please check the file path as it may not be correct.");
				return;
			}
		}
		System.out.println("******************************************");
		System.out.println("******       Input RSpec            ******");
		System.out.println("******************************************");
		System.out.println(rspecString);

		System.out.println("******************************************");
		System.out.println("******     Checking validity        ******");
		System.out.println("******************************************");
		boolean valid = RSpecValidation.validateRspecXMLUnit(rspecString);
		System.out.println("Valid: " + valid + "\n\n");
		if (!valid) {
			System.out.println("RSpec not valid. Quitting process.");
			return;
		}

		System.out.println("******************************************");
		System.out.println("******       Output RSpec           ******");
		System.out.println("******************************************");

		// String type = RSpecValidation.getType(rspecString);
		double errorRate = RSpecValidation.getProportionalError(rspecString);

		System.out.println("Error: " + errorRate);

	}

	private static void validateFile(File path) {

		if (RSpecValidation.rspecFileExtension(path)) {
			String rspecString = null;
			System.out.println(path.getPath().substring(20));
			try {
				rspecString = AbstractConverter.toString(path.getPath()
						.substring(20));
			} catch (IOException e) {
				System.out.println("********* error converting to string ***************");
				e.printStackTrace();
			}
			String type = RSpecValidation.getType(rspecString);

//			boolean validSchemaFactory = RSpecValidation
//					.validateRspecSchemaFactory(path.getPath(), type);
//			System.out.println("validSchemaFactory: " + validSchemaFactory);

			boolean validXMLUnit = RSpecValidation
					.validateRspecXMLUnit(rspecString);
			System.out.println("validXMLUnit: " + validXMLUnit);

			boolean validRSpecLint = RSpecValidation.rspecLintMacOnly(path
					.getPath().substring(20));
			System.out.println("validRSpecLint: " + validRSpecLint);

		}
	}

	public static void main(String[] args) {

		// File path = new File("./src/test/resources/geni/advertisement");
		// File path = new File("./src/test/resources/geni/ciscogeni");
		// File path = new File("./src/test/resources/geni/exogeni");
		// File path = new File("./src/test/resources/geni/fed4fire");
		// File path = new File("./src/test/resources/geni/gimiv3");
		// File path = new File("./src/test/resources/geni/gpolab");
		// File path = new File("./src/test/resources/geni/iminds");
		// File path = new File("./src/test/resources/geni/instageni");
		// File path = new File("./src/test/resources/geni/manifest");
		// File path = new File("./src/test/resources/geni/maxgeni");
		// File path = new File("./src/test/resources/geni/oess");
		// File path = new File("./src/test/resources/geni/openflow");
		// File path = new File("./src/test/resources/geni/productionfoam");
		// File path = new File("./src/test/resources/geni/protogeni");
		// File path = new File("./src/test/resources/geni/request");
		// File path = new File("./src/test/resources/geni/stich");
		
		// File path = new File("./src/test/resources/geni");

		// getErrorDirectory(path);
		// getTimesDirectory(path);
		// validateDirectory(path);
		
		File path = new File("./src/test/resources/geni/exogeni/EG-EXP-5-exp1-openflow-eg-gpo.rspec");
		validateFile(path);
	}
}