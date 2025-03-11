package com.oracle.spring.json.duality.builder;

import com.oracle.spring.json.duality.model.Student;
import org.junit.jupiter.api.Test;

public class ViewEntityTest {
    @Test
    public void studentView() {
        ViewEntity ve = testVE(Student.class);
        String sql = ve.build().toString();

        System.out.println(sql);
    }

    private ViewEntity testVE(Class<?> javaType) {
        return new ViewEntity(javaType, new StringBuilder(), RootSnippet.CREATE, 0);
    }
}
