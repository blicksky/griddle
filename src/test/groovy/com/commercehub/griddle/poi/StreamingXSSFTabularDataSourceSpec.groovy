package com.commercehub.griddle.poi

import com.commercehub.griddle.TabularData
import com.commercehub.griddle.Transformers
import com.commercehub.griddle.poi.streaming.StreamingXSSFTabularDataSource
import spock.lang.Specification

class StreamingXSSFTabularDataSourceSpec extends Specification {

    private static final File FILE1 = getFile("/xssf/spreadsheet1.xlsx")
    private static final File FILE2 = getFile("/xssf/spreadsheet2.xlsx")
    private static final File FILE3 = getFile("/xssf/spreadsheet3.xlsx")
    private static final File FILE4 = getFile("/xssf/spreadsheet4.xlsx")
    private static final File FILE5 = getFile("/xssf/spreadsheet5.xlsx") // sheet with no rows
    private static final File FILE6 = getFile("/xssf/spreadsheet6.xlsx") // sheet with a date and numeric with decimal

    def tabularDataSource = new StreamingXSSFTabularDataSource()

    def "withFile produces one table per worksheet"() {
        int actualTableCount = -1

        when:
        tabularDataSource.withFile(file) { Iterable<TabularData> tables ->
            actualTableCount = tables.collect().size()
        }

        then:
        actualTableCount == expectedTableCount

        where:
        file  | expectedTableCount
        FILE1 | 4
        FILE2 | 1
        FILE3 | 1
        FILE4 | 1
        FILE5 | 2
        FILE6 | 1
    }

    @SuppressWarnings("LineLength")
    def "tables have expected columnNames"() {
        def actualColumnNames = []

        when:
        tabularDataSource.withFile(file) { Iterable<TabularData> tables ->
            for (table in tables) {
                actualColumnNames << table.columnNames.collect()
            }
        }

        then:
        actualColumnNames == expectedColumnNames

        where:
        file  | expectedColumnNames
        FILE1 | [["Foo", "Bar", "Moo", "Goo"], ["Zip-a-Dee-Doo-Dah", "Zip-a-dee-ay"], ["123", "456", "789"], ["Column 1", "Column 2", "Column 4"]]
        FILE2 | [["Zip-a-Dee-Doo-Dah", "Zip-a-dee-ay"]]
        FILE3 | [["123", "456", "789"]]
        FILE4 | [["Column 1", "Column 2", "Column 4"]]
        FILE5 | [["Column A", "Column B"], []]
        FILE6 | [["Column A", "Numeric Data With Decimal", "Date Data"]]
    }

    @SuppressWarnings("LineLength")
    def "table rows have expected values"() {
        def actualRows = []

        when:
        tabularDataSource.withFile(file) { Iterable<TabularData> tables ->
            for (table in tables) {
                actualRows << table.rows.collect()
            }
        }

        then:
        actualRows == expectedRows

        where:
        file  | expectedRows
        FILE1 | [[["Foo":"Lorem", "Bar":"ipsum", "Moo":"dolor", "Goo":"sit"], ["Foo":"12.1", "Bar":"amet", "Moo":"consectetur", "Goo":"7"], ["Foo":"adipisicing", "Bar":"1000", "Moo":"22.124099999999999", "Goo":"elit, sed do"]], [["Zip-a-Dee-Doo-Dah":"My, oh my what a wonderful day!", "Zip-a-dee-ay":"Plenty of sunshine heading my way"], ["Zip-a-Dee-Doo-Dah":"Zip-a-Dee-Doo-Dah", "Zip-a-dee-ay":"Zip-a-dee-ay"]], [["123":"012", "456":"345", "789":"678"], ["123":"9", "456":"10", "789":"11"], ["123":"Aa", "456":"Bb", "789":"Cc"], ["123":"Dd", "456":"Ee", "789":"Ff"]], [["Column 1":"Row 1 Column 1", "Column 4":"Row 1 Column 4"], ["Column 1":"Row 3 Column 1", "Column 2":"Row 3 Column 2", "Column 4":"Row 3 Column 4"], ["Column 1":"Row 4 Column 1", "Column 2":"Row 4 Column 2", "Column 4":"Row 4 Column 4"]]]
        FILE2 | [[["Zip-a-Dee-Doo-Dah":"My, oh my what a wonderful day!", "Zip-a-dee-ay":"Plenty of sunshine heading my way"], ["Zip-a-Dee-Doo-Dah":"Zip-a-Dee-Doo-Dah", "Zip-a-dee-ay":"Zip-a-dee-ay"]]]
        FILE3 | [[["123":"012", "456":"345", "789":"678"], ["123":"9", "456":"10", "789":"11"], ["123":"Aa", "456":"Bb", "789":"Cc"], ["123":"Dd", "456":"Ee", "789":"Ff"]]]
        FILE4 | [[["Column 1":"Row 1 Column 1", "Column 4":"Row 1 Column 4"], ["Column 1":"Row 3 Column 1", "Column 2":"Row 3 Column 2", "Column 4":"Row 3 Column 4"], ["Column 1":"Row 4 Column 1", "Column 2":"Row 4 Column 2", "Column 4":"Row 4 Column 4"]]]
        FILE5 | [[["Column A":"A2", "Column B":"B2"], ["Column A":"A3", "Column B":"B3"]], []]
        FILE6 | [[["Column A":"a", "Numeric Data With Decimal":"21212.4444", "Date Data":"1/12/01"]]]
    }

    @SuppressWarnings("LineLength")
    def "value transformer is used"() {
        def actualRows = []

        when:
        tabularDataSource.setValueTransformer(Transformers.lowercase)
        tabularDataSource.withFile(file) { Iterable<TabularData> tables ->
            for (table in tables) {
                actualRows << table.rows.collect()
            }
        }

        then:
        actualRows == expectedRows

        where:
        file  | expectedRows
        FILE1 | [[["Foo":"lorem", "Bar":"ipsum", "Moo":"dolor", "Goo":"sit"], ["Foo":"12.1", "Bar":"amet", "Moo":"consectetur", "Goo":"7"], ["Foo":"adipisicing", "Bar":"1000", "Moo":"22.124099999999999", "Goo":"elit, sed do"]], [["Zip-a-Dee-Doo-Dah":"my, oh my what a wonderful day!", "Zip-a-dee-ay":"plenty of sunshine heading my way"], ["Zip-a-Dee-Doo-Dah":"zip-a-dee-doo-dah", "Zip-a-dee-ay":"zip-a-dee-ay"]], [["123":"012", "456":"345", "789":"678"], ["123":"9", "456":"10", "789":"11"], ["123":"aa", "456":"bb", "789":"cc"], ["123":"dd", "456":"ee", "789":"ff"]], [["Column 1":"row 1 column 1", "Column 4":"row 1 column 4"], ["Column 1":"row 3 column 1", "Column 2":"row 3 column 2", "Column 4":"row 3 column 4"], ["Column 1":"row 4 column 1", "Column 2":"row 4 column 2", "Column 4":"row 4 column 4"]]]
        FILE2 | [[["Zip-a-Dee-Doo-Dah":"my, oh my what a wonderful day!", "Zip-a-dee-ay":"plenty of sunshine heading my way"], ["Zip-a-Dee-Doo-Dah":"zip-a-dee-doo-dah", "Zip-a-dee-ay":"zip-a-dee-ay"]]]
        FILE3 | [[["123":"012", "456":"345", "789":"678"], ["123":"9", "456":"10", "789":"11"], ["123":"aa", "456":"bb", "789":"cc"], ["123":"dd", "456":"ee", "789":"ff"]]]
        FILE4 | [[["Column 1":"row 1 column 1", "Column 4":"row 1 column 4"], ["Column 1":"row 3 column 1", "Column 2":"row 3 column 2", "Column 4":"row 3 column 4"], ["Column 1":"row 4 column 1", "Column 2":"row 4 column 2", "Column 4":"row 4 column 4"]]]
        FILE5 | [[["Column A":"a2", "Column B":"b2"], ["Column A":"a3", "Column B":"b3"]], []]
        FILE6 | [[["Column A":"a", "Numeric Data With Decimal":"21212.4444", "Date Data":"1/12/01"]]]
    }

    @SuppressWarnings("LineLength")
    def "column transformer is used"() {
        def actualRows = []

        when:
        tabularDataSource.setColumnNameTransformer(Transformers.lowercase)
        tabularDataSource.withFile(file) { Iterable<TabularData> tables ->
            for (table in tables) {
                actualRows << table.rows.collect()
            }
        }

        then:
        actualRows == expectedRows

        where:
        file  | expectedRows
        FILE1 | [[["foo":"Lorem", "bar":"ipsum", "moo":"dolor", "goo":"sit"], ["foo":"12.1", "bar":"amet", "moo":"consectetur", "goo":"7"], ["foo":"adipisicing", "bar":"1000", "moo":"22.124099999999999", "goo":"elit, sed do"]], [["zip-a-dee-doo-dah":"My, oh my what a wonderful day!", "zip-a-dee-ay":"Plenty of sunshine heading my way"], ["zip-a-dee-doo-dah":"Zip-a-Dee-Doo-Dah", "zip-a-dee-ay":"Zip-a-dee-ay"]], [["123":"012", "456":"345", "789":"678"], ["123":"9", "456":"10", "789":"11"], ["123":"Aa", "456":"Bb", "789":"Cc"], ["123":"Dd", "456":"Ee", "789":"Ff"]], [["column 1":"Row 1 Column 1", "column 4":"Row 1 Column 4"], ["column 1":"Row 3 Column 1", "column 2":"Row 3 Column 2", "column 4":"Row 3 Column 4"], ["column 1":"Row 4 Column 1", "column 2":"Row 4 Column 2", "column 4":"Row 4 Column 4"]]]
        FILE2 | [[["zip-a-dee-doo-dah":"My, oh my what a wonderful day!", "zip-a-dee-ay":"Plenty of sunshine heading my way"], ["zip-a-dee-doo-dah":"Zip-a-Dee-Doo-Dah", "zip-a-dee-ay":"Zip-a-dee-ay"]]]
        FILE3 | [[["123":"012", "456":"345", "789":"678"], ["123":"9", "456":"10", "789":"11"], ["123":"Aa", "456":"Bb", "789":"Cc"], ["123":"Dd", "456":"Ee", "789":"Ff"]]]
        FILE4 | [[["column 1":"Row 1 Column 1", "column 4":"Row 1 Column 4"], ["column 1":"Row 3 Column 1", "column 2":"Row 3 Column 2", "column 4":"Row 3 Column 4"], ["column 1":"Row 4 Column 1", "column 2":"Row 4 Column 2", "column 4":"Row 4 Column 4"]]]
        FILE5 | [[["column a":"A2", "column b":"B2"], ["column a":"A3", "column b":"B3"]], []]
        FILE5 | [[["column a":"A2", "column b":"B2"], ["column a":"A3", "column b":"B3"]], []]
        FILE6 | [[["column a":"a", "numeric data with decimal":"21212.4444", "date data":"1/12/01"]]]
    }

    private static File getFile(String resourcePath) {
        return new File(StreamingXSSFTabularDataSourceSpec.getResource(resourcePath).toURI())
    }
}
