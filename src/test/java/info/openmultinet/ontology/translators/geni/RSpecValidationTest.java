package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.RSpecValidation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

public class RSpecValidationTest {

	private static void getErrorDirectory(File path) {

		if (!path.isDirectory()) {
			System.out.println("Not a directory.");
			return;
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
			if (files[i].isFile() && RSpecValidation.rspecFileExtension(files[i])) {
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
	}

	private static void getRoundTripDirectory(File path) {
		if (!path.isDirectory()) {
			System.out.println("Not a directory.");
			return;
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
			if (files[i].isFile() && RSpecValidation.rspecFileExtension(files[i])) {
				String rspecString = null;
				try {
					System.out.println(files[i].getPath());
					System.out.println(files[i].getPath().substring(20));

					// note: substring(20) specifically gets rid of
					// "./src/test/resources"), so must be changed if a
					// different path is used
					rspecString = AbstractConverter.toString(files[i].getPath()
							.substring(20));
					String type = RSpecValidation.getType(rspecString);
					long time = System.nanoTime();
					RSpecValidation.completeRoundtrip(rspecString);
					time = System.nanoTime() - time;
					System.out.println("Time to complete round trip: " + time);

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

					System.out
							.println("==========================================================");
				} catch (IOException e) {
					e.printStackTrace();
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
			System.out.println("Total time for " + times.get(i).size() + " "
					+ type + "s " + interimSum);
			sum += interimSum;
		}

		int numFiles = timeAds.size() + timeManifests.size()
				+ timeRequests.size();
		System.out.println("Total time for " + numFiles + " files: " + sum);
		System.out.println("Comprising " + timeAds.size() + " ads, "
				+ timeManifests.size() + " manifests, and "
				+ timeRequests.size() + " requests.");
		long average = sum / numFiles;
		double seconds = average / 1000000000.0;
		System.out.println("Average time: " + seconds + " seconds");

	}

	private static void validateDirectory(File path) {
		if (!path.isDirectory()) {
			System.out.println("Not a directory.");
			return;
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

		int trueAds = 0;
		int ads = 0;
		int trueManifests = 0;
		int manifests = 0;
		int trueRequests = 0;
		int requests = 0;
		int different = 0;

		for (int i = 0; i < files.length; i++) {

			if (files[i].isFile() && RSpecValidation.rspecFileExtension(files[i])) {
				String rspecString = null;
				try {
					// System.out.println(files[i].getPath());
					System.out.println(files[i].getPath().substring(20));
					rspecString = AbstractConverter.toString(files[i].getPath()
							.substring(20));
					String type = RSpecValidation.getType(rspecString);

					boolean validXMLUnit = RSpecValidation
							.validateRspecXMLUnit(rspecString);
					System.out.println("validXMLUnit: " + validXMLUnit);
					boolean validRSpecLint = RSpecValidation.rspecLintMacOnly(files[i]
							.getPath().substring(20));
					System.out.println("validRSpecLint: " + validRSpecLint);

					switch (type) {
					case "advertisement":
						if (validXMLUnit && validRSpecLint) {
							trueAds++;
						}
						ads++;
						break;
					case "manifest":
						if (validXMLUnit && validRSpecLint) {
							trueManifests++;
						}
						manifests++;
						break;
					case "request":
						if (validXMLUnit && validRSpecLint) {
							trueRequests++;
						}
						requests++;
						break;
					}

					if ((validXMLUnit || validRSpecLint)
							&& !(validXMLUnit && validRSpecLint)) {
						different++;
					}

					System.out
							.println("==========================================================");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Summary: ");
		int total = ads + manifests + requests;
		int totalTrue = trueAds + trueManifests + trueRequests;
		System.out.println(totalTrue + " / " + total + " RSpecs, "
				+ " were valid in both XMLUnit and RSpecLint.");
		System.out.println(trueAds + " / " + ads + " ads were valid");
		System.out.println(trueManifests + " / " + manifests
				+ " manifests were valid");
		System.out.println(trueRequests + " / " + requests
				+ " requests were valid");
		System.out.println("Number where XMLUnit and RSpecLint disagreed: "
				+ different);

	}

	public static void main(String[] args) {

		// File path = new File("./src/test/resources/geni/advertisement");
		// File path = new File("./src/test/resources/geni/fed4fire");
		// File path = new File("./src/test/resources/geni/gimiv3");

		// File path = new File("./src/test/resources/geni/manifest");
		// File path = new File("./src/test/resources/geni/maxgeni");
		// File path = new File("./src/test/resources/geni/protogeni");
		// File path = new File("./src/test/resources/geni/request");
		File path = new File("./src/test/resources/omn/paper2015iswc");

		getErrorDirectory(path);
		// getRoundTripDirectory(path);
		// validateDirectory(path);

	}
}