package org.lxz.analysis;

import com.google.gson.annotations.SerializedName;

/**
 * Class to represent a function name. Function names are specified as
 * db.function_name.
 */
public class FunctionName {
    public static final String GLOBAL_UDF_DB = "__global_udf_db";

    @SerializedName(value = "db")
    private String db_;

    @SerializedName(value = "fn")
    private String fn_;

    private FunctionName() {}

    public FunctionName(String db, String fn) {
        this.db_ = db;
        this.fn_ = fn.toLowerCase();
    }

    public FunctionName(String fn) {
        db_ = null;
        fn_ = fn.toLowerCase();
    }

    public static FunctionName createFnName(String fn) {
        final String[] dbWithFn = fn.split("\\.");
        if (dbWithFn.length == 2) {
            return new FunctionName(dbWithFn[0], dbWithFn[1]);
        } else {
            return new FunctionName(null, fn);
        }
    }


}
