package com.aneesh.information;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class LuceneProject {

	private static PrintWriter outputFile;
	private static final String UTF8BOM = "\uFEFF";
	private static Scanner inputFile;
	private static HashMap<String, LinkedList<Integer>> constructedIndex;

	private static void createInvertedIndex(String url) {
		try {
			Directory directory = FSDirectory.open(FileSystems.getDefault()
					.getPath(url, new String[0]));
			IndexReader indexReader = DirectoryReader.open(directory);
			constructedIndex = new HashMap<String, LinkedList<Integer>>();
			Fields fields = MultiFields.getFields(indexReader);
			String[] lang_fields = { "text_nl", "text_fr", "text_de",
					"text_ja", "text_ru", "text_pt", "text_es", "text_es",
					"text_it", "text_da", "text_no", "text_sv" };

			for (String lang : lang_fields) {
				Terms terms = fields.terms(lang);
				TermsEnum termsEnum = terms.iterator();

				BytesRef token;
				token = termsEnum.next();
				while (token != null) {
					PostingsEnum postingsEnum = MultiFields.getTermDocsEnum(
							indexReader, lang, token);
					LinkedList<Integer> postingsList = new LinkedList<Integer>();
					int i = postingsEnum.nextDoc();
					while (i != PostingsEnum.NO_MORE_DOCS) {
						postingsList.add(i);
						i = postingsEnum.nextDoc();
					}
					constructedIndex.put(token.utf8ToString(), postingsList);
					token = termsEnum.next();
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String[] sortTerms(String[] terms) {
		for (int i = 0; i < terms.length; i++) {
			for (int j = 0; j < terms.length; j++) {
				int count1 = 0, count2 = 0;
				if (constructedIndex.get(terms[i]) != null) {
					count1 = constructedIndex.get(terms[i]).size();
				}
				if (constructedIndex.get(terms[j]) != null) {
					count2 = constructedIndex.get(terms[j]).size();
				}
				String temp;
				if (count1 < count2) {
					temp = terms[i];
					terms[i] = terms[j];
					terms[j] = temp;
				}

			}
		}

		return terms;
	}

	private static void getPostings(String term) {
		outputFile.println("GetPostings");
		outputFile.println(term);
		outputFile.print("Postings list: ");
		LinkedList<Integer> termPostings = constructedIndex.get(term);
		int i = 1;
		for (int doc : termPostings) {
			outputFile.print(Integer.toString(doc));
			if (i < termPostings.size()) {
				outputFile.print(" ");
				i++;
			}
		}
		outputFile.println();
	}

	private static void taatAnd(String[] terms) {
		outputFile.println("TaatAnd");
		for (int i = 0; i < terms.length; i++) {
			outputFile.print(terms[i]);
			if (i != terms.length - 1) {
				outputFile.print(" ");
			}
		}
		outputFile.print("\nResults: ");
		int comparisons = 0;
		terms = sortTerms(terms);
		LinkedList<Integer>[] termPostingsList = new LinkedList[terms.length];
		for (int i = 0; i < terms.length; i++) {
			termPostingsList[i] = constructedIndex.get(terms[i]);
		}
		for (int i = 0; i < terms.length - 1; i++) {
			LinkedList<Integer> tempList = new LinkedList<Integer>();
			int ptr1 = 0;
			int ptr2 = 0;
			while (ptr1 < termPostingsList[i].size()
					&& ptr2 < termPostingsList[i + 1].size()) {
				if (termPostingsList[i].get(ptr1).equals(
						termPostingsList[i + 1].get(ptr2))) {
					tempList.add(termPostingsList[i].get(ptr1));
					ptr1++;
					ptr2++;
				} else if (termPostingsList[i].get(ptr1) < termPostingsList[i + 1]
						.get(ptr2)) {
					ptr1++;
				} else {
					ptr2++;
				}
				comparisons++;
			}
			if (tempList.size() == 0) {
				outputFile.print("empty" + "\n");
				outputFile.println("Number of documents in results: 0");
				outputFile.println("Number of comparisons: " + comparisons);
				return;
			}
			termPostingsList[i + 1] = tempList;
		}

		for (int i = 0; i < termPostingsList[terms.length - 1].size(); i++) {
			outputFile.print(termPostingsList[terms.length - 1].get(i));

			if (i != termPostingsList[terms.length - 1].size() - 1) {
				outputFile.print(" ");
			}
		}
		outputFile.println("\nNumber of documents in results: "
				+ termPostingsList[terms.length - 1].size());
		outputFile.println("Number of comparisons: " + comparisons);
	}

	private static void taatOr(String terms[]) {
		outputFile.println("TaatOr");
		for (int i = 0; i < terms.length; i++) {
			outputFile.print(terms[i]);
			if (i != terms.length - 1) {
				outputFile.print(" ");
			}
		}
		outputFile.print("\nResults: ");
		int comparisons = 0;
		terms = sortTerms(terms);
		LinkedList<Integer>[] termPostingsList = new LinkedList[terms.length];
		for (int i = 0; i < terms.length; i++) {
			termPostingsList[i] = constructedIndex.get(terms[i]);
		}
		for (int i = 0; i < terms.length - 1; i++) {
			LinkedList<Integer> tempList = new LinkedList<Integer>();
			int ptr1 = 0;
			int ptr2 = 0;
			while (ptr1 < termPostingsList[i].size()
					&& ptr2 < termPostingsList[i + 1].size()) {
				if (termPostingsList[i].get(ptr1).equals(
						termPostingsList[i + 1].get(ptr2))) {
					tempList.add(termPostingsList[i].get(ptr1));
					ptr1++;
					ptr2++;
				} else if (termPostingsList[i].get(ptr1) < termPostingsList[i + 1]
						.get(ptr2)) {
					tempList.add(termPostingsList[i].get(ptr1));
					ptr1++;
				} else {
					tempList.add(termPostingsList[i + 1].get(ptr2));
					ptr2++;
				}
				comparisons++;
			}

			while (ptr1 < termPostingsList[i].size()) {
				tempList.add(termPostingsList[i].get(ptr1));
				ptr1++;
			}

			while (ptr2 < termPostingsList[i + 1].size()) {
				tempList.add(termPostingsList[i + 1].get(ptr2));
				ptr2++;
			}

			termPostingsList[i + 1] = tempList;
		}

		for (int i = 0; i < termPostingsList[terms.length - 1].size(); i++) {
			outputFile.print(termPostingsList[terms.length - 1].get(i));

			if (i != termPostingsList[terms.length - 1].size() - 1) {
				outputFile.print(" ");
			}
		}
		outputFile.println("\nNumber of documents in results: "
				+ termPostingsList[terms.length - 1].size());
		outputFile.println("Number of comparisons: " + comparisons);
	}

	private static void daatAnd(String terms[]) {
		outputFile.println("DaatAnd");
		for (int i = 0; i < terms.length; i++) {
			outputFile.print(terms[i]);
			if (i != terms.length - 1) {
				outputFile.print(" ");
			}
		}
		outputFile.print("\nResults: ");
		int comparisons = 0;
		terms = sortTerms(terms);
		LinkedList<Integer>[] termPostingsList = new LinkedList[terms.length];
		for (int i = 0; i < terms.length; i++) {
			termPostingsList[i] = constructedIndex.get(terms[i]);
		}
		int[] ptr = new int[terms.length];
		for (int i = 0; i < terms.length; i++) {
			ptr[i] = 0;
		}
		boolean access = true;
		LinkedList<Integer> tempList = new LinkedList<Integer>();
		while (access) {
			int current_doc = termPostingsList[0].get(ptr[0]);
			boolean include_current = true, advance_list = true;
			for (int i = 1; i < terms.length; i++) {
				if (termPostingsList[i].get(ptr[i]) == current_doc) {
					ptr[i]++;
				} else if (termPostingsList[i].get(ptr[i]) < current_doc) {
					include_current = false;
					advance_list = false;
					ptr[i]++;
				} else if (termPostingsList[i].get(ptr[i]) > current_doc) {
					include_current = false;
				}
				comparisons++;
			}

			if (include_current) {
				tempList.add(current_doc);
			}
			if (advance_list) {
				ptr[0]++;
			}

			for (int i = 0; i < terms.length; i++) {
				if (ptr[i] == termPostingsList[i].size()) {
					access = false;
					break;
				}
			}
		}

		if (tempList.size() == 0) {
			outputFile.print("empty");
		} else {
			for (int i = 0; i < tempList.size(); i++) {
				outputFile.print(tempList.get(i));

				if (i != tempList.size() - 1) {
					outputFile.print(" ");
				}
			}
		}

		outputFile.println("\nNumber of documents in results: "
				+ tempList.size());
		outputFile.println("Number of comparisons: " + comparisons);
	}

	private static void daatOr(String terms[]) {
		outputFile.println("DaatOr");
		for (int i = 0; i < terms.length; i++) {
			outputFile.print(terms[i]);
			if (i != terms.length - 1) {
				outputFile.print(" ");
			}
		}
		outputFile.print("\nResults: ");
		int comparisons = 0;
		terms = sortTerms(terms);
		LinkedList<Integer>[] termPostingsList = new LinkedList[terms.length];
		for (int i = 0; i < terms.length; i++) {
			termPostingsList[i] = constructedIndex.get(terms[i]);
		}
		int[] ptr = new int[terms.length];
		boolean[] finished = new boolean[terms.length];
		for (int i = 0; i < terms.length; i++) {
			ptr[i] = 0;
			finished[i] = false;
		}
		boolean access = true;

		LinkedList<Integer> tempList = new LinkedList<Integer>();
		while (access) {
			int current_doc = Integer.MAX_VALUE;
			int current_i = Integer.MAX_VALUE;
			for (int i = 0; i < terms.length; i++) {
				if (!finished[i]) {
					if (current_doc > termPostingsList[i].get(ptr[i])) {
						current_doc = termPostingsList[i].get(ptr[i]);
						current_i = i;
					}
				}
			}
			for (int i = 0; i < terms.length; i++) {
				if (!finished[i]) {
					if (termPostingsList[i].get(ptr[i]) == current_doc) {
						ptr[i]++;
					}
					if (current_i != i)
						comparisons++;
				}
			}

			tempList.add(current_doc);

			for (int i = 0; i < terms.length; i++) {
				if (ptr[i] == termPostingsList[i].size()) {
					finished[i] = true;
				}
			}

			access = false;

			for (int i = 0; i < terms.length; i++) {
				if (!finished[i]) {
					access = true;
				}
			}
		}

		for (int i = 0; i < tempList.size(); i++) {
			outputFile.print(tempList.get(i));

			if (i != tempList.size() - 1) {
				outputFile.print(" ");
			}
		}
		outputFile.println("\nNumber of documents in results: "
				+ tempList.size());
		outputFile.println("Number of comparisons: " + comparisons);
	}

	private static void processQueries(String inputPath) {
		try {
			inputFile = new Scanner(new InputStreamReader(new FileInputStream(
					inputPath), "UTF-8"));
			boolean firstLine = true;

			while (inputFile.hasNextLine()) {
				String input = inputFile.nextLine();
				if (firstLine) {
					firstLine = false;
					input = removeBOM(input);
				}
				String[] queryTerms = input.split(" ");

				for (String queryTerm : queryTerms) {
					getPostings(queryTerm);
				}
				taatAnd(queryTerms);
				taatOr(queryTerms);
				daatAnd(queryTerms);
				daatOr(queryTerms);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String removeBOM(String input) {
		if (input.startsWith(UTF8BOM)) {
			input = input.substring(1);
		}
		return input;
	}

	public static void main(String args[]) {

		String indexUrl = null, inputFileUrl = null, outputFileUrl = null;

		try {
			indexUrl = args[0];
			outputFileUrl = args[1];
			inputFileUrl = args[2];
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		try {
			outputFile = new PrintWriter(new File(outputFileUrl));
		} catch (Exception e) {
			e.printStackTrace();
		}
		createInvertedIndex(indexUrl);
		processQueries(inputFileUrl);
		outputFile.close();
	}
}
