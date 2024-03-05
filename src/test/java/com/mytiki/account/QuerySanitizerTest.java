package com.mytiki.account;

import com.mytiki.account.utilities.QuerySanitizer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QuerySanitizerTest {

    @Test
    public void Test_sanitize_Success(){
        QuerySanitizer query = new QuerySanitizer();
        String mockQuery1 = " SELECT test_object FROM tiki.test_table from test; ";
        String mockQuery2 = " SELECT test_object from test_table FROM tiki.test; ";

        assertEquals("SELECT test_object FROM tiki.test_table from tiki.test", query.sanitize(mockQuery1));
        assertEquals("SELECT test_object from tiki.test_table FROM tiki.test", query.sanitize(mockQuery2));
    }
}
