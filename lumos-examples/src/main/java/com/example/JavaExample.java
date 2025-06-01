package com.example;

// Assuming com.lumos.runtime.Lumen is the data class that will be injected.
// If the package or class name is different, please adjust.
import com.lumos.runtime.Lumen; // Ensure this import is correct

public class JavaExample {
    public void targetMethodInJava(Lumen lumen, int value) {
        System.out.println("JavaExample.targetMethodInJava called:");
        System.out.println("  Value: " + value);
        System.out.println("  Lumen filePath: " + lumen.getFilePath());
        System.out.println("  Lumen fileName: " + lumen.getFileName());
        System.out.println("  Lumen lineNumber: " + lumen.getLineNumber());
        System.out.println("  Lumen targetFunctionName: " + lumen.getTargetFunctionName());
        // Add other Lumen fields if they exist and are relevant for MVP
    }
}
