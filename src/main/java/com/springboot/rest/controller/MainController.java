package com.springboot.rest.controller;

import com.springboot.rest.model.ResponseJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@RestController
public class MainController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/count")
    public String getCount() {
        String result = "";
        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select count(*) from source_data");
            if (rs != null && rs.next()) {
                result = "Count rows : " + rs.getInt(1);
                rs.close();
            }
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            result = "Error";
        }
        return result;
    }

    @GetMapping(value = "/row={row}&col={col}", produces = "application/json")
    public List<ResponseJson> getPivot(@PathVariable String col, @PathVariable String row) {
        List<ResponseJson> responseJsonList = new ArrayList<>();
        String query = getQuery(row, col);
        if (query != null) {
            try {
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(query);
                if (rs != null) {
                    while (rs.next()) {
                        ResponseJson responseJson = new ResponseJson();
                        responseJson.setRow(rs.getString("row"));
                        responseJson.setCol(rs.getString("col"));
                        responseJson.setVal(rs.getLong("val"));
                        responseJsonList.add(responseJson);
                    }
                    rs.close();
                }
                statement.close();
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return responseJsonList;
    }

    private String getQuery(String row, String col) {
        String query = "";
        if (isAllowSymbol(row) && isAllowSymbol(col)) {
            String params = row + ", " + col;
            query = "select " + row + " as row, " + col + " as col, sum(v) as val from source_data group by " + params + " order by " + params;
            return query;
        }
        return null;
    }

    private boolean isAllowSymbol(String param) {
        if (param.equals("a") || param.equals("b") || param.equals("c") || param.equals("d") || param.equals("y"))
            return true;
        else return false;
    }
}
