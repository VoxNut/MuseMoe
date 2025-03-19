package com.javaweb.builder;

import lombok.Getter;

@Getter
public class ProductSearchBuilder {
    private String name;
    private String type;
    private Integer status;

    private ProductSearchBuilder(Builder builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.status = builder.status;
    }


    public static class Builder {
        private String name;
        private String type;
        private Integer status;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setStatus(Integer status) {
            this.status = status;
            return this;
        }


        public ProductSearchBuilder build() {
            ProductSearchBuilder builder = new ProductSearchBuilder(this);
            return builder;
        }


    }

}
