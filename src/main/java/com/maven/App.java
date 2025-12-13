package com.maven;

public class App {
    public static void main(String[] args) {
        System.out.println("Hello from Maven!");
        System.out.println("Build successful!");
        System.out.println("Jenkins pipeline is working!");
    }
    
    public String getMessage() {
        return "Hello Maven";
    }
}