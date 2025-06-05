package org.lxz.catalog;

import com.google.gson.annotations.SerializedName;
import org.lxz.analysis.Expr;
import org.lxz.analysis.FunctionName;
import org.lxz.common.Pair;

import java.util.Vector;

/**
 * Base class for all functions.
 */
public class Function {


    // Enum for how to compare function signatures.
    // For decimal types, the type in the function can be a wildcard, i.e. decimal(*.*).
    // The wildcard can *only* exist as function type, the caller will always be a
    // fully specified decimal.
    // For the purposes of function type resolution, decimal(*,*) will match exactly
    // with any fully specified decimal (i.e. fn(decimal(*.*)) matches identically for
    // the call to fn(decimal(1,0)).
    public enum CompareMode {
        // Two signatures are identical if the number of arguments and their types match
        // exactly and either both signatures are varargs or neither.
        IS_IDENTICAL,

        // Two signatures are indistinguishable if there is no way to tell them apart
        // when matching a particular instantiation. That is, their fixed arguments
        // match exactly and the remaining varargs have the same type.
        // e.g. fn(int, int, int) and fn(int...)
        // Argument types that are NULL are ignored when doing this comparison.
        // e.g. fn(NULL, int) is indistinguishable from fn(int, int)
        IS_INDISTINGUISHABLE,


        // X is a supertype of Y if Y.arg[i] can be strictly implicitly cast to X.arg[i]. If
        // X has vargs, the remaining arguments of Y must be strictly implicitly castable
        // to the var arg type. The key property this provides is that X can be used in place
        // of Y. e.g. fn(int, double, string...) is a supertype of fn(tinyint, float, string,
        // string)
        IS_SUPERTYPE_OF,

        // Nonstrict supertypes broaden the definition of supertype to accept implicit casts
        // of arguments that may result in loss of precision - e.g. decimal to float.
        IS_NONSTRICT_SUPERTYPE_OF,

    }

    // for vectorized engine, function-id
    @SerializedName(value = "fid")
    protected long functionId;

    @SerializedName(value = "name")
    protected FunctionName name;

    @SerializedName(value = "retType")
    protected Type retType;

    // Array of parameter types. empty array if this function does not have parameters.
    @SerializedName(value = "argTypes")
    protected Type[] argTypes;

    @SerializedName(value = "argNames")
    protected String[] argNames;

    // If true, this function have variable arguments.
    @SerializedName(value = "hasVarArgs")
    protected boolean hasVarArgs;

    // If true(default), this function is called directly by the user. For operators,
    // this is false, If false, it also means the function is not visible from
    // 'show functions'.
    @SerializedName(value = "userVisible")
    protected boolean userVisible;


    // todo support thrift later
    // @SerializedName(value = "binaryType")

    // hdfs location

    // library's checksum to make sure all backends use one library to serve user's request
    @SerializedName(value = "checksum")
    protected String checksum = "";

    // Function id, every function has a unique id. Now all built-in functions' id is 0
    private long id = 0;
    // User specified function name e.g. "Add"

    private boolean isPolymorphic = false;

    // If low cardinality string column with global dict, for some string functions,
    // we could evaluate the function only with the dict content, not all string column data.
    private boolean couldApplyDictOptimize = false;

    private boolean isNullable = true;

    private Vector<Pair<String, Expr>> defaultArgExprs;

    private boolean isMetaFunction = false;

    public Type getRetType() {
        return retType;
    }
}
