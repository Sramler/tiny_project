package com.example.sort;

/**
 * 冒泡排序实现类
 * 冒泡排序是一种简单的排序算法，它重复地遍历要排序的数组，比较相邻元素并交换它们的位置
 */
public class BubbleSort {
    
    /**
     * 冒泡排序方法
     * @param arr 待排序的整数数组
     */
    public static void bubbleSort(int[] arr) {
        // 获取数组长度
        int n = arr.length;
        
        // 外层循环控制排序轮数
        for (int i = 0; i < n - 1; i++) {
            // 内层循环进行相邻元素比较和交换
            for (int j = 0; j < n - 1 - i; j++) {
                // 如果当前元素大于下一个元素，则交换它们的位置
                if (arr[j] > arr[j + 1]) {
                    // 交换元素
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }

    /**
     * 主方法，用于测试冒泡排序
     */
    public static void main(String[] args) {
        // 创建测试数组
        int[] arr = {64, 34, 25, 12, 22, 11, 90};
        
        // 打印原始数组
        System.out.println("排序前的数组：");
        printArray(arr);
        
        // 执行冒泡排序
        bubbleSort(arr);
        
        // 打印排序后的数组
        System.out.println("排序后的数组：");
        printArray(arr);
    }
    
    /**
     * 打印数组的辅助方法
     * @param arr 要打印的数组
     */
    public static void printArray(int[] arr) {
        // 遍历数组并打印每个元素
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i] + " ");
        }
        System.out.println();
    }
} 