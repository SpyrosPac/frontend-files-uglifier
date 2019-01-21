uglifyJavascript = function (code) {

    var result =  UglifyJS.minify(code);
    return result.code;
};