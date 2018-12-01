<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
	<meta name="viewport" content="width=device-width">
	<title> Basic example </title>
	<link rel="stylesheet" href="treant/Treant.css">
	<link rel="stylesheet" href="css/collapsable.css">
	<link rel="stylesheet" href="treant/examples/basic-example/basic-example.css">

	<style type="text/css" media="screen">
		#code {
			position: absolute; /* Added */
			top: 100px;
			right: 60%;
			bottom: 50%;
			left: 50px;

		}

		#comments {
			position: absolute; /* Added */
			top: 51%;
			right:60%;
			bottom: 50px;
			left: 50px;
		}

	</style>
</head>

<body>

	<script src="treant/vendor/raphael.js"></script>
	<script src="treant/Treant.js"></script>

	<script src="basic-example.js"></script>


	<div id="exampleNavigator" style="position:absolute; top:50px; left:50px; right:50px;height:50px">
		<div style="text-Align:center;margin:0 auto;">
			<button type="button" style="text-Align:center;margin:0 auto;" onclick="onClickFormerExample()">former example</button>
			<label id="currentExample"style="text-Align:center;margin:0 auto;">0</label>
			<button type="button" style="text-Align:center;margin:0 auto;" onclick="onClickLatterExample()">latter example</button>
		</div>
	</div>


	<div id="code">BooleanQuery a1AndT1 = new BooleanQuery();
a1AndT1.add(new TermQuery(new Term("Author", "a1")), BooleanClause.Occur.MUST);
a1AndT1.add(new TermQuery(new Term("title", "t1")), BooleanClause.Occur.MUST);
BooleanQuery a2AndT2 = new BooleanQuery();
a2AndT2.add(new TermQuery(new Term("Author", "a2")), BooleanClause.Occur.MUST);
a2AndT2.add(new TermQuery(new Term("title", "t2")), BooleanClause.Occur.MUST);
BooleanQuery a3AndT3 = new BooleanQuery();
a3AndT3.add(new TermQuery(new Term("Author", "a3")), BooleanClause.Occur.MUST);
a3AndT3.add(new TermQuery(new Term("title", "t3")), BooleanClause.Occur.MUST);
BooleanQuery query = new BooleanQuery();
query.add(a1AndT1, BooleanClause.Occur.SHOULD);
query.add(a2AndT2, BooleanClause.Occur.SHOULD);
query.add(a3AndT3, BooleanClause.Occur.SHOULD);
System.out.println(query);
	</div>

	<div id="comments">Create a BooleanQuery for (Author:a1 and title:t1)
Create a BooleanQuery for (Author:a2 and title:t2)
Create a BooleanQuery for (Author:a3 and title:t3)
Create a BooleanQuery that combines the OR-clauses
As you can see, the resulting Lucene query is
(+Author:a1 +title:t1) (+Author:a2 +title:t2) (+Author:a3 +title:t3)
which behaves the same as something like
(Author:a1 and title:t1) OR (Author:a2 and title:t2) OR (Author:a3 and title:t3)
	</div>

	<div id="displayArea" style="position:absolute; top:100px;left:40%;bottom:50px;right:50px">
		<div style="width:100%">
			<div style="text-Align:center;margin:0 auto;">
				<button type="button" style="text-Align:center;margin:0 auto;" onclick="onClickFormerComment()">former comment</button>
				<label id="currentCodeLine"style="text-Align:center;margin:0 auto;">0</label>
				<button type="button" style="text-Align:center;margin:0 auto;" onclick="onClickLatterComment()">latter comment</button>
			</div>
		</div>
		<div class = "chart" id =  "displayResult" style="width:100%">

			<%
				int exampleNum;
				int commentNum;
				String chart;
				String codes ;
				String comments;
				try {
					exampleNum = Integer.parseInt(request.getParameter("exampleNum"));
					commentNum = Integer.parseInt(request.getParameter("commentNum"));
					chart =
					codes = request.getParameter("code");
					comments = request.getParameter("comment");

				}catch(Exception e){
				    exampleNum = commentNum = 0;
				    chart = "";
				    codes = "";
				    comments = "";

				}


			%>

			<script>

				/*console.print("yes");
				var exampleNum = request.getParameter("exampleNum");
				var commentNum = request.getParameter("commentNum");
				var chart = request.getParameter("chart");

				if(code == null){
				    document.getElementById("currentExample").value(0);
				    document.getElementById("currentCodeLine").value(0);
				}else{
                    document.getElementById("currentExample").value(exampleNum);
                    document.getElementById("currentCodeLine").value(comment);

                    var codeLines = chart["codeLines"];
                    var comments = chart["comments"];

                    document.getElementById("comments").value(comments);
                    document.getElementById("code").value(codeLines);
				}

				var b =
                var test = {"nodeStructure":{"children":[{"collapsed":"false","text":{"1":"BooleanQuery","2":"---","3":"1 2: a1AndT1","4":"2 2: a1AndT1","5":"4 2: a2AndT2","6":"5 2: a2AndT2","7":"7 2: a3AndT3","8":"8 2: a3AndT3"}},{"children":[{"children":[{"collapsed":"false","text":{"1":"title","2":"---","3":"2 17: \"title\""}},{"collapsed":"false","text":{"1":"t1","2":"---","3":"2 19: \"t1\""}}],"collapsed":"true","text":{"1":"a1  and  title  :  t1","2":"---","3":"2 12: new Term(\"title\",\"t1\")"}}],"collapsed":"true","text":{"1":"for  (  Author  :  a1  and  title  :  t1  )","2":"---","3":"1 12: new Term(\"Author\",\"a1\")"}}],"collapsed":"true","text":{"1":"Create  a  BooleanQuery  for  (  Author  :  a1  and  title  :  t1  )","2":"---","3":"1 1: a1AndT1.add(new TermQuery(new Term(\"Author\",\"a1\")),BooleanClause.Occur.MUST)","4":"2 1: a1AndT1.add(new TermQuery(new Term(\"title\",\"t1\")),BooleanClause.Occur.MUST)","5":"4 1: a2AndT2.add(new TermQuery(new Term(\"Author\",\"a2\")),BooleanClause.Occur.MUST)","6":"5 1: a2AndT2.add(new TermQuery(new Term(\"title\",\"t2\")),BooleanClause.Occur.MUST)","7":"7 1: a3AndT3.add(new TermQuery(new Term(\"Author\",\"a3\")),BooleanClause.Occur.MUST)","8":"8 1: a3AndT3.add(new TermQuery(new Term(\"title\",\"t3\")),BooleanClause.Occur.MUST)"}},"chart":{"container":"#displayResult","node":{"collapsable":"true"},"animation":{"nodeAnimation":"easeOutBounce","connectorsAnimation":"bounce","connectorsSpeed":700,"nodeSpeed":700}}}
                tree = new Treant( test );*/
			</script>
		</div>
	</div>

	<script src="ace/src-noconflict/ace.js" type="text/javascript" charset="utf-8"></script>
	<script>
        var editor = ace.edit("code");
        editor.setTheme("ace/theme/twilight");
        editor.session.setMode("ace/mode/java");

        var editor1 = ace.edit("comments");
        editor.setTheme("ace/theme/twilight");
        editor.session.setMode("ace/mode/java");
	</script>
	<script src="js/index.js"></script>
	<script src="js/jquery.min.js"></script>
	<script src="js/jquery.slimscroll.min.js"></script>
</body>
</html>
</html>