/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.quorum.tests.compiler.Random;

import java.util.TimeZone;
import java.util.Date;
import org.quorum.execution.ExpressionValue;
import org.quorum.tests.compiler.CompilerTestSuite;
import org.quorum.vm.implementation.QuorumVirtualMachine;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jeff Wilson
 */
public class RandomTester {
    private QuorumVirtualMachine vm;
    public RandomTester() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        CompilerTestSuite.forceSetup();
        vm = CompilerTestSuite.getVirtualMachine();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void test_SetSeed() {
        CompilerTestSuite.build(CompilerTestSuite.getQuorumFile(CompilerTestSuite.RANDOM + CompilerTestSuite.PASS + "SetSeed.quorum"));
        if (!vm.getCompilerErrors().isCompilationErrorFree()){
            fail();
        }
    }
    
    @Test
    public void test_RandomInteger() {
        CompilerTestSuite.build(CompilerTestSuite.getQuorumFile(CompilerTestSuite.RANDOM + CompilerTestSuite.PASS + "RandomInteger.quorum"));
        if (!vm.getCompilerErrors().isCompilationErrorFree()){
            fail();
        }
        vm.blockRun();
        
        ExpressionValue variableValue = vm.getDataEnvironment().getVariableValue("result");
        int a = variableValue.getResult().integer;
        if(a != 544657192) {
            fail();
        }   
    }
    
    @Test
    public void test_RandomIntegerWithInvalidMaximum() {
        CompilerTestSuite.build(CompilerTestSuite.getQuorumFile(CompilerTestSuite.RANDOM + CompilerTestSuite.FAIL + "RandomInteger.quorum"));
        if (!vm.getCompilerErrors().isCompilationErrorFree()){
            fail();
        }
        vm.blockRun();
        
        if (!vm.getExceptions().hasAlerts()) {
            fail();
        }
    }
    
    @Test
    public void test_RandomIntegerBetweenWithSameValues() {
        CompilerTestSuite.build(CompilerTestSuite.getQuorumFile(CompilerTestSuite.RANDOM + CompilerTestSuite.FAIL + "RandomIntegerBetweenWithSameValues.quorum"));
        if (!vm.getCompilerErrors().isCompilationErrorFree()){
            fail();
        }
        vm.blockRun();
        
        if (!vm.getExceptions().hasAlerts()) {
            fail();
        }
    }
    
    @Test
    public void test_RandomIntegerBetweenWithInvalidRange() {
        CompilerTestSuite.build(CompilerTestSuite.getQuorumFile(CompilerTestSuite.RANDOM + CompilerTestSuite.FAIL + "RandomIntegerBetweenWithInvalidRange.quorum"));
        if (!vm.getCompilerErrors().isCompilationErrorFree()){
            fail();
        }
        vm.blockRun();
        
        if (!vm.getExceptions().hasAlerts()) {
            fail();
        }
    }
    
    @Test
    public void test_RandomIntegerBetweenWithNegativeNumbers() {
        CompilerTestSuite.build(CompilerTestSuite.getQuorumFile(CompilerTestSuite.RANDOM + CompilerTestSuite.FAIL + "RandomIntegerBetweenWithNegativeNumbers.quorum"));
        if (!vm.getCompilerErrors().isCompilationErrorFree()){
            fail();
        }
        vm.blockRun();
        
        if (!vm.getExceptions().hasAlerts()) {
            fail();
        }
    }
    
    @Test
    public void test_RandomIntegerWithMaximum() {
        int numberOfResults = 10;
        int[] expectedResults = {7, 12, 7, 9, 6, 13, 14, 7, 6, 3};
        
        CompilerTestSuite.build(CompilerTestSuite.getQuorumFile(CompilerTestSuite.RANDOM + CompilerTestSuite.PASS + "RandomIntegerWithMaximum.quorum"));
        if (!vm.getCompilerErrors().isCompilationErrorFree()){
            fail();
        }
        vm.blockRun();
        
        // Somewhat hackish: Loop through variable result0 through result9 and make
        // sure they match the expectedResults.
        for (int i = 0; i < numberOfResults; i++) {
            ExpressionValue variableValue = vm.getDataEnvironment().getVariableValue("result" + i);
            int a = variableValue.getResult().integer;
            if(a != expectedResults[i]) {
                fail();
            }   
        }
    }
    
    @Test
    public void test_RandomIntegerBetween() {
        int numberOfResults = 10;
        int[] expectedResults = {17, 16, 19, 17, 19, 16, 17, 19, 17, 16};
        
        CompilerTestSuite.build(CompilerTestSuite.getQuorumFile(CompilerTestSuite.RANDOM + CompilerTestSuite.PASS + "RandomIntegerBetween.quorum"));
        if (!vm.getCompilerErrors().isCompilationErrorFree()){
            fail();
        }
        vm.blockRun();
        
        // Somewhat hackish: Loop through variable result0 through result9 and make
        // sure they match the expectedResults.
        for (int i = 0; i < numberOfResults; i++) {
            ExpressionValue variableValue = vm.getDataEnvironment().getVariableValue("result" + i);
            int a = variableValue.getResult().integer;
            if(a != expectedResults[i]) {
                fail();
            }   
        }
    }
    
    @Test
    public void test_RandomListOfNumbers() {
        CompilerTestSuite.build(CompilerTestSuite.getQuorumFile(CompilerTestSuite.RANDOM + CompilerTestSuite.PASS + "RandomListOfNumbers.quorum"));
        if (!vm.getCompilerErrors().isCompilationErrorFree()){
            fail();
        }
        vm.blockRun();


        ExpressionValue aValue = vm.getDataEnvironment().getVariableValue("a");
        ExpressionValue bValue = vm.getDataEnvironment().getVariableValue("b");
        ExpressionValue cValue = vm.getDataEnvironment().getVariableValue("c");
        ExpressionValue dValue = vm.getDataEnvironment().getVariableValue("d");
        ExpressionValue eValue = vm.getDataEnvironment().getVariableValue("e");
        ExpressionValue sizeValue = vm.getDataEnvironment().getVariableValue("size");
        ExpressionValue emptyValue = vm.getDataEnvironment().getVariableValue("empty");
        
        double a = aValue.getResult().number;
        double b = bValue.getResult().number;
        double c = cValue.getResult().number;
        double d = dValue.getResult().number;
        double e = eValue.getResult().number;
        int size = sizeValue.getResult().integer;
        boolean empty = emptyValue.getResult().boolean_value;
        
        // Ugly code: make sure all the values match the expected ones
        if (a != 0.2536257672785417 || b != 0.7691565943833395 || c != 0.9048039800926115
            || d != 0.3874507370773981 || e != 0.3108243382133744)
        {
            fail();
        }
        
        // The iterator should be empty and the size should be 5.
        if (!empty || size != 5) {
            fail();
        }
    }
    
    @Test
    public void test_RandomListOfIntegers() {
        CompilerTestSuite.build(CompilerTestSuite.getQuorumFile(CompilerTestSuite.RANDOM + CompilerTestSuite.PASS + "RandomListOfIntegers.quorum"));
        if (!vm.getCompilerErrors().isCompilationErrorFree()){
            fail();
        }
        vm.blockRun();


        ExpressionValue aValue = vm.getDataEnvironment().getVariableValue("a");
        ExpressionValue bValue = vm.getDataEnvironment().getVariableValue("b");
        ExpressionValue cValue = vm.getDataEnvironment().getVariableValue("c");
        ExpressionValue dValue = vm.getDataEnvironment().getVariableValue("d");
        ExpressionValue eValue = vm.getDataEnvironment().getVariableValue("e");
        ExpressionValue sizeValue = vm.getDataEnvironment().getVariableValue("size");
        ExpressionValue emptyValue = vm.getDataEnvironment().getVariableValue("empty");
        
        int a = aValue.getResult().integer;
        int b = bValue.getResult().integer;
        int c = cValue.getResult().integer;
        int d = dValue.getResult().integer;
        int e = eValue.getResult().integer;
        int size = sizeValue.getResult().integer;
        boolean empty = emptyValue.getResult().boolean_value;
        
        // Ugly code: make sure all the values match the expected ones
        if (a != 544657192 || b != 264551322 || c != 1651751212 || d != 616702974
            || e != 1943051751)
        {
            fail();
        }
        
        // The iterator should be empty and the size should be 5.
        if (!empty || size != 5) {
            fail();
        }
    }
    
    @Test
    public void test_RandomListOfNumbersWithInvalidLength() {
        CompilerTestSuite.build(CompilerTestSuite.getQuorumFile(CompilerTestSuite.RANDOM + CompilerTestSuite.FAIL + "RandomListOfNumbersWithInvalidLength.quorum"));
        
        if (!vm.getCompilerErrors().isCompilationErrorFree()){
            fail();
        }
        vm.blockRun();
        
        if (!vm.getExceptions().hasAlerts()) {
            fail();
        }
    }

    @Test
    public void test_RandomListOfIntegersWithInvalidLength() {
        CompilerTestSuite.build(CompilerTestSuite.getQuorumFile(CompilerTestSuite.RANDOM + CompilerTestSuite.FAIL + "RandomListOfIntegersWithInvalidLength.quorum"));
        if (!vm.getCompilerErrors().isCompilationErrorFree()){
            fail();
        }
        vm.blockRun();

        
        if (!vm.getExceptions().hasAlerts()) {
            fail();
        }
    }
}
