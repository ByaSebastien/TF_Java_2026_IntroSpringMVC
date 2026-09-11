package be.bstorm.tf_java_2026_introspringmvc.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode @ToString
public class Category {

    private static Long nextId = 1L;

    @Getter
    private Long id;

    @Getter @Setter
    private String name;

    public Category() {
        this.id = nextId++;
    }

    public Category(String name) {
        this();
        this.name = name;
    }
}
