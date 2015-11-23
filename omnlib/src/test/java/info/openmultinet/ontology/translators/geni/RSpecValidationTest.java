package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.exceptions.DeprecatedRspecVersionException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;

public class RSpecValidationTest {

	final static int trueAds = 0;
	final static int ads = 1;
	final static int trueManifests = 2;
	final static int manifests = 3;
	final static int trueRequests = 4;
	final static int requests = 5;
	final static int different1 = 6;
	final static int different2 = 7;
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

		String pathToscaRdfxml = "./src/test/resources/tosca/newTubitTosca.xml";
		Entry<String, String> pair5 = new java.util.AbstractMap.SimpleEntry<>(
				AbstractConverter.RDFXML, pathToscaRdfxml);
		types.add(pair5);

		String pathRequestPrefix = "./src/test/resources/geni/request/request_namespace.xml";
		Entry<String, String> pair6 = new java.util.AbstractMap.SimpleEntry<>(
				AbstractConverter.RSPEC_REQUEST, pathRequestPrefix);
		types.add(pair6);

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

	private static ArrayList<List<Double>> getErrorDirectory(File path)
			throws MissingRspecElementException,
			DeprecatedRspecVersionException, FileNotFoundException,
			InvalidRspecValueException {

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

	private static ArrayList<List<Integer>> getNodesDiffsDirectory(File path)
			throws MissingRspecElementException,
			DeprecatedRspecVersionException, FileNotFoundException,
			InvalidRspecValueException {

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

		List<Integer> diffsAds = new ArrayList<Integer>();
		List<Integer> diffsManifests = new ArrayList<Integer>();
		List<Integer> diffsRequests = new ArrayList<Integer>();
		ArrayList<List<Integer>> totalDiffNodes = new ArrayList<List<Integer>>();
		totalDiffNodes.add(diffsAds);
		totalDiffNodes.add(diffsManifests);
		totalDiffNodes.add(diffsRequests);

		List<Integer> inNodesAds = new ArrayList<Integer>();
		List<Integer> inNodesManifests = new ArrayList<Integer>();
		List<Integer> inNodesRequests = new ArrayList<Integer>();
		totalDiffNodes.add(inNodesAds);
		totalDiffNodes.add(inNodesManifests);
		totalDiffNodes.add(inNodesRequests);

		List<Integer> outNodesAds = new ArrayList<Integer>();
		List<Integer> outNodesManifests = new ArrayList<Integer>();
		List<Integer> outNodesRequests = new ArrayList<Integer>();
		totalDiffNodes.add(outNodesAds);
		totalDiffNodes.add(outNodesManifests);
		totalDiffNodes.add(outNodesRequests);

		List<Integer> attributesInAds = new ArrayList<Integer>();
		List<Integer> attributesInManifests = new ArrayList<Integer>();
		List<Integer> attributesInRequests = new ArrayList<Integer>();
		totalDiffNodes.add(attributesInAds);
		totalDiffNodes.add(attributesInManifests);
		totalDiffNodes.add(attributesInRequests);

		List<Integer> attributesOutAds = new ArrayList<Integer>();
		List<Integer> attributesOutManifests = new ArrayList<Integer>();
		List<Integer> attributesOutRequests = new ArrayList<Integer>();
		totalDiffNodes.add(attributesOutAds);
		totalDiffNodes.add(attributesOutManifests);
		totalDiffNodes.add(attributesOutRequests);

		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()
					&& RSpecValidation.rspecFileExtension(files[i])) {
				String rspecString = null;
				// boolean valid = false;
				try {
					System.out.println(files[i].getPath().substring(20));
					rspecString = AbstractConverter.toString(files[i].getPath()
							.substring(20));
					// valid =
					// RSpecValidation.validateRspecXMLUnit(rspecString);
					// System.out.println("Valid: " + valid);
				} catch (IOException e) {
					e.printStackTrace();
				}

				System.out.println(files[i].getPath().substring(20));
				System.out.println(files[i].getPath());
				// if (valid) {

				// note: substring(20) specifically gets rid of
				// "./src/test/resources"), so must be changed if a
				// different path is used

				String type = RSpecValidation.getType(rspecString);
				int[] diffNodes = RSpecValidation.getDiffsNodes(rspecString);
				System.out.println("Diffs: " + diffNodes[0]);
				System.out.println("Nodes: " + diffNodes[1]);

				switch (type) {
				case "advertisement":
					diffsAds.add(diffNodes[0]);
					inNodesAds.add(diffNodes[1]);
					outNodesAds.add(diffNodes[2]);
					attributesInAds.add(diffNodes[3]);
					attributesOutAds.add(diffNodes[4]);
					break;
				case "manifest":
					diffsManifests.add(diffNodes[0]);
					inNodesManifests.add(diffNodes[1]);
					outNodesManifests.add(diffNodes[2]);
					attributesInManifests.add(diffNodes[3]);
					attributesOutManifests.add(diffNodes[4]);
					break;
				case "request":
					diffsRequests.add(diffNodes[0]);
					inNodesRequests.add(diffNodes[1]);
					outNodesRequests.add(diffNodes[2]);
					attributesInRequests.add(diffNodes[3]);
					attributesOutRequests.add(diffNodes[4]);
					break;
				}
				// }
			} else if (files[i].isDirectory()) {
				ArrayList<List<Integer>> newTotalDiffNodes = getNodesDiffsDirectory(files[i]);
				for (int j = 0; j < totalDiffNodes.size(); j++) {
					totalDiffNodes.get(j).addAll(newTotalDiffNodes.get(j));
				}
			}
			System.out
					.println("==========================================================");
		}

		int sumInNodes = 0;
		int sumOutNodes = 0;
		int sumDiffs = 0;
		int sumAttributesIn = 0;
		int sumAttributesOut = 0;

		for (int i = 0; i < 3; i++) {
			int interimSumInNodes = 0;
			int interimSumOutNodes = 0;
			int interimSumDiffs = 0;
			int interimAttributesIn = 0;
			int interimAttributesOut = 0;

			for (int j = 0; j < totalDiffNodes.get(i).size(); j++) {
				interimSumDiffs += totalDiffNodes.get(i).get(j);
				interimSumInNodes += totalDiffNodes.get(i + 3).get(j);
				interimSumOutNodes += totalDiffNodes.get(i + 6).get(j);
				interimAttributesIn += totalDiffNodes.get(i + 9).get(j);
				interimAttributesOut += totalDiffNodes.get(i + 12).get(j);
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
			System.out.println("Total diffs for "
					+ totalDiffNodes.get(i).size() + " " + type + "s "
					+ interimSumDiffs);
			System.out.println("Total in nodes for "
					+ totalDiffNodes.get(i).size() + " " + type + "s "
					+ interimSumInNodes);
			System.out.println("Total out nodes for "
					+ totalDiffNodes.get(i).size() + " " + type + "s "
					+ interimSumOutNodes);
			System.out.println("Total in attributes for "
					+ totalDiffNodes.get(i).size() + " " + type + "s "
					+ interimAttributesIn);
			System.out.println("Total out nodes for "
					+ totalDiffNodes.get(i).size() + " " + type + "s "
					+ interimAttributesOut);
			sumDiffs += interimSumDiffs;
			sumInNodes += interimSumInNodes;
			sumOutNodes += interimSumOutNodes;
			sumAttributesIn += interimAttributesIn;
			sumAttributesOut += interimAttributesOut;
		}

		int numFiles = diffsAds.size() + diffsManifests.size()
				+ diffsRequests.size();
		System.out.println("Total diffs for " + numFiles + " files: "
				+ sumDiffs);
		System.out.println("Total in nodes for " + numFiles + " files: "
				+ sumInNodes);
		System.out.println("Total out nodes for " + numFiles + " files: "
				+ sumOutNodes);
		System.out.println("Total attributes in for " + numFiles + " files: "
				+ sumAttributesIn);
		System.out.println("Total attributes out for " + numFiles + " files: "
				+ sumAttributesOut);
		System.out.println("Comprising " + diffsAds.size() + " ads, "
				+ diffsManifests.size() + " manifests, and "
				+ diffsRequests.size() + " requests.");
		double averageDiffs = sumDiffs / numFiles;
		double averageInNodes = sumInNodes / numFiles;
		double averageOutNodes = sumOutNodes / numFiles;
		double averageAttributesIn = sumAttributesIn / numFiles;
		double averageAttributesOut = sumAttributesOut / numFiles;
		System.out.println("Average diffs per file: " + averageDiffs);
		System.out.println("Average in nodes per file: " + averageInNodes);
		System.out.println("Average out nodes per file: " + averageOutNodes);
		System.out.println("Average attributes in per file: "
				+ averageAttributesIn);
		System.out.println("Average attributes out per file: "
				+ averageAttributesOut);

		return totalDiffNodes;
	}

	private static ArrayList<List<Long>> getTimesDirectory(File path)
			throws MissingRspecElementException,
			DeprecatedRspecVersionException, InvalidRspecValueException {
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

					if (type != null) {
						switch (type) {
						case "advertisement":
							// if (validSchemaFactory && validRSpecLint) {
							if (validXMLUnit && validRSpecLint) {
								// if (validRSpecLint) {
								valid[trueAds]++;
							}
							valid[ads]++;
							break;
						case "manifest":
							// if (validSchemaFactory && validRSpecLint) {
							if (validXMLUnit && validRSpecLint) {
								// if (validRSpecLint) {
								valid[trueManifests]++;
							}
							valid[manifests]++;
							break;
						case "request":
							// if (validSchemaFactory && validRSpecLint) {
							if (validXMLUnit && validRSpecLint) {
								// if (validRSpecLint) {
								valid[trueRequests]++;
							}
							valid[requests]++;
							break;
						}
					}
					// if ((validXMLUnit || validRSpecLint)
					// && !(validXMLUnit && validRSpecLint)) {
					if (validXMLUnit && !validRSpecLint) {
						valid[different1]++;
					}

					if (!validXMLUnit && validRSpecLint) {
						valid[different2]++;
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
		int total = valid[ads] + valid[manifests] + valid[requests];
		int totalTrue = valid[trueAds] + valid[trueManifests]
				+ valid[trueRequests];
		int totalDisagree = valid[different1] + valid[different2];
		System.out.println(totalTrue + " / " + total + " RSpecs, "
				+ " were valid in both XMLUnit and RSpecLint.");
		System.out.println(valid[trueAds] + " / " + valid[ads]
				+ " ads were valid");
		System.out.println(valid[trueManifests] + " / " + valid[manifests]
				+ " manifests were valid");
		System.out.println(valid[trueRequests] + " / " + valid[requests]
				+ " requests were valid");
		System.out.println("Number where XMLUnit and RSpecLint disagreed: "
				+ totalDisagree);
		System.out
				.println("Number where valid in XMLUnit and not valid in RSpecLint: "
						+ valid[different1]);
		System.out
				.println("Number where not valid in XMLUnit and valid in RSpecLint: "
						+ valid[different2]);

		return valid;

	}

	private static void getErrorFile(File path)
			throws MissingRspecElementException,
			DeprecatedRspecVersionException, IOException,
			InvalidRspecValueException {

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
		// boolean valid = RSpecValidation.validateRspecXMLUnit(rspecString);

		boolean valid = RSpecValidation.rspecLintMacOnly(path.getPath()
				.substring(20));
		System.out.println("Valid: " + valid + "\n\n");
		if (!valid) {
			System.out.println("RSpec not valid. Quitting process.");
			return;
		}

		System.out.println("******************************************");
		System.out.println("******       Output RSpec           ******");
		System.out.println("******************************************");

		// String type = RSpecValidation.getType(rspecString);
		// double errorRate = RSpecValidation.getProportionalError(rspecString);
		int[] diffNodes = RSpecValidation.getDiffsNodes(rspecString);

		// System.out.println("Error: " + errorRate);
		System.out.println("Diffs: " + diffNodes[0]);
		System.out.println("Nodes: " + diffNodes[1]);

	}

	public static void validateFile(File path) {

		if (RSpecValidation.rspecFileExtension(path)) {
			String rspecString = null;
			System.out.println(path.getPath().substring(20));
			try {
				rspecString = AbstractConverter.toString(path.getPath()
						.substring(20));
			} catch (IOException e) {
				e.printStackTrace();
			}

			boolean validSchemaFactory = false;
			boolean validRSpecLint = false;

			if (rspecString != null) {
				String type = RSpecValidation.getType(rspecString);
				validSchemaFactory = RSpecValidation
						.validateRspecSchemaFactory(path.getPath(), type);
				validRSpecLint = RSpecValidation.rspecLintMacOnly(path
						.getPath().substring(20));
			}

			System.out.println("validSchemaFactory: " + validSchemaFactory);

			boolean validXMLUnit = RSpecValidation
					.validateRspecXMLUnit(rspecString);
			System.out.println("validXMLUnit: " + validXMLUnit);

			System.out.println("validRSpecLint: " + validRSpecLint);

		}
	}

	private static int[] checkVersionDirectory(File path) {
		System.out
				.println("==========================================================");
		System.out
				.println("===========  checkVersionDirectory test           ============");

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

		int[] valid = new int[9];

		for (int i = 0; i < files.length; i++) {

			if (files[i].isFile()
					&& RSpecValidation.rspecFileExtension(files[i])) {
				valid[8]++;
				String rspecString = null;
				try {
					// System.out.println(files[i].getPath());
					System.out.println(files[i].getPath().substring(20));
					rspecString = AbstractConverter.toString(files[i].getPath()
							.substring(20));
					String type = RSpecValidation.getType(rspecString);

					boolean validRspecVersion = RSpecValidation
							.checkVersion(rspecString);
					System.out.println("valid version: " + validRspecVersion);

					if (type != null) {
						switch (type) {
						case "advertisement":
							if (validRspecVersion) {
								valid[ads]++;
							}
							break;
						case "manifest":
							if (validRspecVersion) {
								valid[manifests]++;
							}
							break;
						case "request":
							if (validRspecVersion) {
								valid[requests]++;
							}
							break;
						}
					}

					System.out
							.println("==========================================================");
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (files[i].isDirectory()) {
				int[] newValid = checkVersionDirectory(files[i]);
				for (int j = 0; j < valid.length; j++) {
					valid[j] = newValid[j] + valid[j];
				}
			}
		}
		System.out.println("Summary: ");
		int total = valid[ads] + valid[manifests] + valid[requests];

		System.out.println(total + " of " + valid[8] + " RSpecs, "
				+ " had a valid version.");
		System.out.println(valid[ads] + " ads had a valid version");
		System.out.println(valid[manifests] + " manifests had a valid version");
		System.out.println(valid[requests] + " requests had a valid version");

		return valid;

	}

	// public static void main(String[] args) throws
	// MissingRspecElementException,
	// DeprecatedRspecVersionException, IOException {
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
	// checkVersionDirectory(path);
	// getErrorDirectory(path);
	// getNodesDiffsDirectory(path);
	// getTimesDirectory(path);
	// validateDirectory(path);
	// File path = new File(
	// "./src/test/resources/geni/request/request_bound.xml");
	// File path = new File(
	// "./src/test/resources/geni/exogeni/EG-EXP-5-exp1-openflow-eg-gpo.rspec");
	// File path = new File(
	// "./src/test/resources/geni/ciscogeni/CG-CT-5-eg-ncsu2.rspec");
	// File path = new File(
	// "./src/test/resources/geni/ciscogeni/CG-CT-5-openflow-eg-ncsu2.rspec");
	// validateFile(path);
	// File path = new File(
	// "./src/test/resources/geni/advertisement/advertisement_vwall1.xml");
	// getErrorFile(path);
	//
	// String rspecString = null;
	// try {
	// System.out.println(path.getPath().substring(20));
	// rspecString = AbstractConverter.toString(path.getPath().substring(
	// 20));
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// int[] diffNodes = RSpecValidation.getDiffsNodes(rspecString);
	//
	// }
}