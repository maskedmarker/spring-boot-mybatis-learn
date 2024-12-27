


# DefaultSqlSession

ParamNameResolver
```text
    public static Object wrapToMapIfCollection(Object object, String actualParamName) {
        ParamMap map;
        if (object instanceof Collection) {
            map = new ParamMap();
            map.put("collection", object);
            if (object instanceof List) {
                map.put("list", object);
            }

            Optional.ofNullable(actualParamName).ifPresent((name) -> {
                map.put(name, object);
            });
            return map;
        } else if (object != null && object.getClass().isArray()) {
            map = new ParamMap();
            map.put("array", object);
            Optional.ofNullable(actualParamName).ifPresent((name) -> {
                map.put(name, object);
            });
            return map;
        } else {
            return object;
        }
    }
```

