package com.trihydro.library.helpers.deserializers;

import com.trihydro.library.models.Product;

public class ProductDeserializer extends ListDeserializer<Product> {

    @Override
    Class<Product> getClazz() {
        return Product.class;
    }

    @Override
    String getProxyName() {
        return "product";
    }

}