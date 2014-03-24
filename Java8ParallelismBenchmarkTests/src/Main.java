import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class Main {

	private static final String FILE_PATH = "resource/Results.xls";
	private static final int limit = 10000;

	public static void main(String[] args) throws IOException,
			InterruptedException {
		// create Workbook
		Workbook wb = XLSUtil.createWorkbook();

		// create Sheet
		Sheet scenario1Sheet = wb.createSheet("Scenario 1");
		Sheet scenario2Sheet = wb.createSheet("Scenario 2");

		// create Column
		XLSUtil.createColumn(scenario1Sheet);// row 0
		XLSUtil.createColumn(scenario2Sheet);// row 0

		// Start Scenario 1

		// run 3 seq test 10 times
		List<Integer[]> randomArrayList = new ArrayList<Integer[]>();
		for (int i = 0; i < 10; i++) {
			Integer[] integerArray = Utility.generateRandomNumber(limit);
			randomArrayList.add(integerArray);
			Row row = XLSUtil.createRow(scenario1Sheet, i + 1);

			// 1. seq sort using traditional java
			Integer[] arrayToSort = integerArray.clone();
			long startTime = System.nanoTime();
			Arrays.sort(arrayToSort);
			long endTime = System.nanoTime();
			long duration = endTime - startTime;
			// store duration of seq sort
			row.createCell(0).setCellValue(duration);

			// 2. seq reduce using traditional java
			List<Integer> arrayToReduce = Arrays.asList(integerArray.clone());
			startTime = System.nanoTime();
			List<Integer> primaryNumberList = new ArrayList<Integer>();
			List<Integer> nonPrimaryNumberList = new ArrayList<Integer>();
			Map<Boolean, List<Integer>> reducedMap = new HashMap<Boolean, List<Integer>>();
			for (Integer temp : arrayToReduce) {
				if (Utility.isPrime(temp)) {
					primaryNumberList.add(temp);
				} else
					nonPrimaryNumberList.add(temp);
			}
			reducedMap.put(true, primaryNumberList);
			reducedMap.put(false, nonPrimaryNumberList);
			endTime = System.nanoTime();
			duration = endTime - startTime;
			row.createCell(1).setCellValue(duration);

			// 3. seq filter using traditional java
			List<Integer> arrayToFilter = Arrays.asList(integerArray.clone());
			startTime = System.nanoTime();
			List<Integer> notPrimaryNumberList = new ArrayList<Integer>();
			for (Integer temp : arrayToFilter) {
				if (!Utility.isPrime(temp)) {
					notPrimaryNumberList.add(temp);
				}
			}
			Integer[] notPrimaryArray = new Integer[notPrimaryNumberList.size()];
			notPrimaryArray = (Integer[]) notPrimaryNumberList
					.toArray(notPrimaryArray);
			endTime = System.nanoTime();
			duration = endTime - startTime;
			row.createCell(2).setCellValue(duration);

		}

		// run 3 Parallel test 10 times
		for (int i = 0; i < 10; i++) {
			Integer[] integerArray = randomArrayList.get(i);
			Row row = scenario1Sheet.getRow(i + 1);
			// 4. Parallel filter using java 8 Parallism
			Integer[] arrayToParallelSort = integerArray.clone();
			long startTime = System.nanoTime();
			Arrays.parallelSort(arrayToParallelSort);
			long endTime = System.nanoTime();
			long duration = endTime - startTime;
			row.createCell(3).setCellValue(duration);

			// 5. Parallel Reduction using java 8 Parallism
			List<Integer> arrayToParallelReduce = Arrays.asList(integerArray
					.clone());
			startTime = System.nanoTime();
			Map<Boolean, List<Integer>> groupByIsPrimary = arrayToParallelReduce
					.stream().collect(
							Collectors.groupingBy(s -> true == Utility
									.isPrime(s)));
			endTime = System.nanoTime();
			duration = endTime - startTime;
			row.createCell(4).setCellValue(duration);

			// 6. Parallel filter using java 8 Parallism
			List<Integer> arrayToParallelFilter = Arrays.asList(integerArray
					.clone());
			startTime = System.nanoTime();
			Integer[] notPrims = arrayToParallelFilter.parallelStream()
					.filter(s -> false == Utility.isPrime(s))
					.toArray(Integer[]::new);
			endTime = System.nanoTime();
			duration = endTime - startTime;
			row.createCell(5).setCellValue(duration);
		}
		// End Scenario 1

		// Start Scenario 2
		Thread[] sortThreads = new Thread[10];
		Thread[] reduceThreads = new Thread[10];
		Thread[] filterThreads = new Thread[10];

		// Open 10 thread for Seq 3 test
		for (int i = 1; i <= 10; i++) {
			Integer[] integerArray = randomArrayList.get(i - 1);
			Row row = XLSUtil.createRow(scenario2Sheet, i);
			Integer[] arrayToSort = integerArray.clone();
			List<Integer> arrayToReduce = Arrays.asList(integerArray.clone());
			List<Integer> arrayToFilter = Arrays.asList(integerArray.clone());
			sortThreads[i - 1] = new Thread(new Runnable() {

				@Override
				public void run() {

					// 1. seq sort using traditional java
					long startTime = System.nanoTime();
					Arrays.sort(arrayToSort);
					long endTime = System.nanoTime();
					long duration = endTime - startTime;
					// store duration of seq sort
					row.createCell(0).setCellValue(duration);

				}
			});
			reduceThreads[i - 1] = new Thread(new Runnable() {

				@Override
				public void run() {

					// 2. seq reduce using traditional java
					long startTime = System.nanoTime();
					List<Integer> primaryNumberList = new ArrayList<Integer>();
					List<Integer> nonPrimaryNumberList = new ArrayList<Integer>();
					Map<Boolean, List<Integer>> reducedMap = new HashMap<Boolean, List<Integer>>();
					for (Integer temp : arrayToReduce) {
						if (Utility.isPrime(temp)) {
							primaryNumberList.add(temp);
						} else
							nonPrimaryNumberList.add(temp);
					}
					reducedMap.put(true, primaryNumberList);
					reducedMap.put(false, nonPrimaryNumberList);
					long endTime = System.nanoTime();
					long duration = endTime - startTime;
					row.createCell(1).setCellValue(duration);

				}
			});
			filterThreads[i - 1] = new Thread(new Runnable() {

				@Override
				public void run() {

					// 3. seq filter using traditional java
					long startTime = System.nanoTime();
					List<Integer> notPrimaryNumberList = new ArrayList<Integer>();
					for (Integer temp : arrayToFilter) {
						if (!Utility.isPrime(temp)) {
							notPrimaryNumberList.add(temp);
						}
					}
					Integer[] notPrimaryArray = new Integer[notPrimaryNumberList
							.size()];
					notPrimaryArray = (Integer[]) notPrimaryNumberList
							.toArray(notPrimaryArray);
					long endTime = System.nanoTime();
					long duration = endTime - startTime;
					row.createCell(2).setCellValue(duration);

				}
			});

			sortThreads[i - 1].start();
			reduceThreads[i - 1].start();
			filterThreads[i - 1].start();
		}

		// wait for threads to finish
		for (Thread thread : sortThreads) {
			thread.join();
		}
		for (Thread thread : reduceThreads) {
			thread.join();
		}
		for (Thread thread : filterThreads) {
			thread.join();
		}

		// Open 10 thread for Parallel 3 test
		for (int i = 1; i <= 10; i++) {
			Integer[] integerArray = randomArrayList.get(i - 1);
			Row row = scenario2Sheet.getRow(i);
			Integer[] arrayToParallelSort = integerArray.clone();
			List<Integer> arrayToParallelReduce = Arrays.asList(integerArray
					.clone());
			List<Integer> arrayToParallelFilter = Arrays.asList(integerArray
					.clone());

			sortThreads[i - 1] = new Thread(new Runnable() {

				@Override
				public void run() {

					// 4. Parallel filter using java 8 Parallism
					long startTime = System.nanoTime();
					Arrays.parallelSort(arrayToParallelSort);
					long endTime = System.nanoTime();
					long duration = endTime - startTime;
					row.createCell(3).setCellValue(duration);

				}
			});

			reduceThreads[i - 1] = new Thread(new Runnable() {

				@Override
				public void run() {

					// 5. Parallel Reduction using java 8 Parallism
					long startTime = System.nanoTime();
					Map<Boolean, List<Integer>> groupByIsPrimary = arrayToParallelReduce
							.stream().collect(
									Collectors.groupingBy(s -> true == Utility
											.isPrime(s)));
					long endTime = System.nanoTime();
					long duration = endTime - startTime;
					row.createCell(4).setCellValue(duration);
				}
			});

			filterThreads[i - 1] = new Thread(new Runnable() {

				@Override
				public void run() {

					// 6. Parallel filter using java 8 Parallism
					long startTime = System.nanoTime();
					Integer[] notPrims = arrayToParallelFilter.parallelStream()
							.filter(s -> false == Utility.isPrime(s))
							.toArray(Integer[]::new);
					long endTime = System.nanoTime();
					long duration = endTime - startTime;
					row.createCell(5).setCellValue(duration);
				}
			});

			sortThreads[i - 1].start();
			reduceThreads[i - 1].start();
			filterThreads[i - 1].start();
		}

		// wait for threads to finish
		for (Thread thread : sortThreads) {
			thread.join();
		}
		for (Thread thread : reduceThreads) {
			thread.join();
		}
		for (Thread thread : filterThreads) {
			thread.join();
		}

		// Save Workbook
		XLSUtil.autoSizeColumn(scenario1Sheet, 6);
		XLSUtil.autoSizeColumn(scenario2Sheet, 6);
		XLSUtil.writeToXls(wb, FILE_PATH);

	}
}
