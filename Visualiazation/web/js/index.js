function onClickFormerExample(){
    var codeLine = Number(document.getElementById("currentExample").innerHTML) - 1;
    var comment = 0;
    chooseExample(codeLine , comment);

}

function onClickLatterExample(){
    var codeLine = Number(document.getElementById("currentExample").innerHTML ) + 1;
    var comment = 0;
    chooseExample(codeLine , comment);
}

function onClickFormerComment(){
    var codeLine = Number(document.getElementById("currentExample").innerHTML) ;
    var comment = Number(document.getElementById("currentCodeLine").innerHTML) - 1;
    chooseExample(codeLine , comment);

}

function onClickLatterComment(){
    var codeLine = Number(document.getElementById("currentExample").innerHTML) ;
    var comment = Number(document.getElementById("currentCodeLine").innerHTML) + 1;
    chooseExample(codeLine , comment);
}

function chooseExample(codeLine , comment){
    var parameters ={
        codeLine : codeLine,
        comment : comment
    };

    $.ajax({
        type:"Post",
        url:"ExampleChoose",
        data : parameters,
        async : false,
        success : function(data){
            //window.location.href = "https://www.baidu.com/"
            document.cookie = "chart=" + data["chart"];

            //window.location.href = "index.html?exampleNum=" + data["exampleNum"] + "&" + "commentNum=" + data["commentNum"] + "&"
            //+ "comment=" + data["comment"] + "&" + "code=" + data["code"] + "&" + "chart=" + data["chart"];
            var exampleNum = data["exampleNum"];
            var commentNum = data["commentNum"];
            var comment = data["comment"];
            var code = data["code"];
            var chart = data["chart"];

            document.getElementById("currentExample").innerHTML = exampleNum;
            document.getElementById("currentCodeLine").innerHTML = commentNum;
            // document.getElementById("comments").innerHTML = comment;
            // document.getElementById("code").innerHTML = code;

            var tree = new Treant(data["chart"]);

            var editor = ace.edit("code");
            editor.setValue(code);
            // editor.setTheme("../ace/theme/twilight");
            // editor.session.setMode("../ace/mode/java");
            //
            var editor1 = ace.edit("comments");
            editor1.setValue(comment);
            // editor.setTheme("../ace/theme/twilight");
            // editor.session.setMode("../ace/mode/java");
        }
    });
}
