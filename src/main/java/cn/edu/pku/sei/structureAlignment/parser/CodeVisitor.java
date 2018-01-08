package cn.edu.pku.sei.structureAlignment.parser;

import cn.edu.pku.sei.structureAlignment.tree.CodeStructureTree;
import cn.edu.pku.sei.structureAlignment.tree.Node;
import cn.edu.pku.sei.structureAlignment.tree.NodeType;
import cn.edu.pku.sei.structureAlignment.tree.Tree;
import org.eclipse.jdt.core.dom.*;


import java.util.ArrayList;
import java.util.List;

import static org.eclipse.jdt.core.dom.InfixExpression.Operator.TIMES;


/**
 * Created by oliver on 2017/12/24.
 */
public class CodeVisitor extends ASTVisitor {
    protected CodeStructureTree tree;
    protected List<Tree> children;
    private static int id ;
    private Tree parent ; // this is used to set the parent of the tree


    //this constructor only be used when we need to construct a new codeStructTree
    public CodeVisitor(int id){
        this.id = id;
        children = new ArrayList<Tree>();
        this.parent = null;

    }

    //this constructor only be used inside codeVisitor ,
    //it means when we try to construct the sub-part of a tree, we use this constructor to build the sub-tree

    // usually , the top node a tree will be encoded with 0 ,
    // and there is a static id, which is used to store the number which will be used for next node.
    private CodeVisitor(Tree parent){
        children = new ArrayList<Tree>();
        this.parent = parent;
    }

    public CodeStructureTree getTree(){
        return this.tree;
    }

    private List<CodeStructureTree> batchProcess(List<ASTNode> nodes , String separator , NodeType separatorType , CodeStructureTree parentTree){
        List<CodeStructureTree> result = new ArrayList<CodeStructureTree>();

        CodeStructureTree separatorTree = null;

        for(ASTNode node : nodes){
            if(separatorTree != null){
                result.add(separatorTree);
                id ++;
            }

            CodeVisitor nodeVisitor = new CodeVisitor(parentTree);
            node.accept(nodeVisitor);
            result.add(nodeVisitor.getTree());

            Node separatorRoot = new Node(separatorType , separator , id);
            separatorTree = new CodeStructureTree(separatorRoot , separator , parentTree);
        }

        return result;
    }

    @Override
    public boolean visit(AnnotationTypeDeclaration node) {
        return false;
    }

    @Override
    public boolean visit(AnnotationTypeMemberDeclaration node) {
        return false;
    }

    @Override
    public boolean visit(AnonymousClassDeclaration node) {
        return false;
    }

    @Override
    public boolean visit(ArrayAccess node) {
        //grammar: Expression '[' Expression ']'

        //region <construct the tree of root>
        Node root = new Node(NodeType.CODE_ArrayAccess , "" , id ++);
        this.tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of root>

        //region <construct the tree of the first Expression>
        Expression arrayExpression = node.getArray();
        CodeVisitor arrayVisitor = new CodeVisitor(this.tree);
        arrayExpression.accept(arrayVisitor);
        children.add(arrayVisitor.getTree());
        //endregion <construct the tree of the first Expression>

        //region <construct the tree of [ >
        Node lbRoot = new Node(NodeType.ADDED_CHAR_LEFT_BRACKET , "[" , id ++);
        CodeStructureTree lbTree = new CodeStructureTree(lbRoot , "[" , this.tree);
        children.add(lbTree);
        //endregion <construct the tree of [>


        //region <construct the tree of the index>
        Expression indexExpression = node.getIndex();
        CodeVisitor indexVisitor = new CodeVisitor(tree);
        indexExpression.accept(indexVisitor);
        CodeStructureTree indexTree = indexVisitor.getTree();
        children.add(indexTree);
        //endregion <construct the tree of index>

        //region <construct the tree of ] >
        Node rbRoot = new Node(NodeType.ADDED_CHAR_LEFT_BRACKET , "]" , id ++);
        CodeStructureTree rbTree = new CodeStructureTree(lbRoot , "]" , tree);
        children.add(rbTree);
        //endregion <construct the tree of [>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(ArrayCreation node) {
        // region <grammar>
        /**
         * new PrimitiveType '[' Expression ']' { '[' Expression ']' } { '[' ']' }
         *
         * new TypeName [ < Type { , Type } > ] '[' Expression ']' { '[' Expression ']' } { '[' ']' }
         *
         * new PrimitiveType '[' ']' { '[' ']' } ArrayInitializer
         *
         * new TypeName [ < Type { , Type } > ] '[' ']' { '[' ']' } ArrayInitializer
         */
        // endregion <grammar>

        /** note
         * for example , the node represents the code "new int[6][5][]"
         * node.getType() will return  "int [][]" , and use the return node n , call the function n.getElementType() will return int
         *                                          and use the return node n , call the function n.dimensions().size will return 3
         * however , the node.dimensions() will return list of expressions ,  in this expression ,will return {6 ,5}
         *
         */


        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_ArrayCreation , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of new>
        Node newRoot =new Node(NodeType.ADDED_KEYWORD , "new" , id ++);
        CodeStructureTree newTree = new CodeStructureTree(newRoot , "new" , tree);
        children.add(newTree);
        //endregion <construct the tree of new>

        //region <construct the tree of type and dimensions>
        ASTNode type = node.getType().getElementType();
        int dimensionLength = node.getType().dimensions().size();
        List<ASTNode> dimensionWidths = node.dimensions();


        //region <construct the tree of type>
        CodeVisitor typeVisitor = new CodeVisitor(tree);
        type.accept(typeVisitor);
        children.add(typeVisitor.getTree());
        //endregion <construct the tree of type>

        int d = 0;
        for( ; d < dimensionWidths.size() ; d++){
            //region <construct the tree of [ >
            Node lbRoot = new Node(NodeType.ADDED_CHAR_LEFT_BRACKET , "[" , id ++);
            CodeStructureTree lbTree = new CodeStructureTree(lbRoot , "[" , tree);
            children.add(lbTree);
            //endregion <construct the tree of [ >

            //region <construct the tree of dimension>
            ASTNode dimension = dimensionWidths.get(d);
            CodeVisitor dimensionVisitor = new CodeVisitor(tree);
            dimension.accept(dimensionVisitor);
            children.add(dimensionVisitor.getTree());
            //endregion <construct the tree of dimension>

            //region <construct the tree of ] >
            Node rbRoot = new Node(NodeType.ADDED_CHAR_RIGHT_BRACKET , "]" , id ++);
            CodeStructureTree rbTree = new CodeStructureTree(rbRoot , "]" , tree);
            children.add(rbTree);
            //endregion <construct the tree of ] >
        }
        while(d < dimensionLength){
            //region <construct the tree of [ >
            Node lbRoot = new Node(NodeType.ADDED_CHAR_LEFT_BRACKET , "[" , id ++);
            CodeStructureTree lbTree = new CodeStructureTree(lbRoot , "[" , tree);
            children.add(lbTree);
            //endregion <construct the tree of [ >

            //region <construct the tree of ] >
            Node rbRoot = new Node(NodeType.ADDED_CHAR_RIGHT_BRACKET , "]" , id ++);
            CodeStructureTree rbTree = new CodeStructureTree(rbRoot , "]" , tree);
            children.add(rbTree);
            //endregion <construct the tree of ] >

            d ++;
        }


        //endregion <construct the tree of type and dimensions>

        //region <construct the tree of ArrayInitializer>
        ASTNode initializer = node.getInitializer();
        CodeVisitor initializerVisitor = new CodeVisitor(tree);
        initializer.accept(initializerVisitor);
        children.add(initializerVisitor.getTree());
        //endregion <construct the tree of ArrayInitializer>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(ArrayInitializer node) {
        //region <grammar>
        /**
         * '{'[ Expression { , Expression} [ , ]] '}'
         */
        //endregion <grammar>

        //region <construct the tree of root>
        Node root = new Node(NodeType.CODE_ArrayInitializer , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent );
        //endregion <construct the tree of root>

        //region <construct the tree of {>""
        Node lbRoot = new Node(NodeType.ADDED_CHAR_LEFT_BRACE , "{" , id ++);
        CodeStructureTree lbTree = new CodeStructureTree(lbRoot , "{" , tree);
        children.add(lbTree);
        //endregion <construct the tree of {>

        //region <construct the tree of expressions>
        List<ASTNode> expressions = node.expressions();
        List<CodeStructureTree> expressionTrees = batchProcess(expressions , "," , NodeType.ADDED_CHAR_COMMA , tree);
        children.addAll(expressionTrees);
        //endregion <construct the tree of expressions>

        //region <construct the tree of }>""
        Node rbRoot = new Node(NodeType.ADDED_CHAR_LEFT_BRACE , "}" , id ++);
        CodeStructureTree rbTree = new CodeStructureTree(rbRoot , "}" , tree);
        children.add(rbTree);
        //endregion <construct the tree of }>


        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(ArrayType node) {
        //region <grammar>
        /**
         * Type Dimension {Dimension}
         */
        //endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_ArrayType , "" , id ++);
        this.tree = new CodeStructureTree(root , node.toString() , parent);
        children = new ArrayList<Tree>();
        //endregion <construct the tree of the root>

        //region <construct the tree of the Type>
        Type type = node.getElementType();
        CodeVisitor typeVisitor = new CodeVisitor(tree);
        type.accept(typeVisitor);
        children.add(typeVisitor.getTree());
        //endregion <construct the tree of the Type>

        //region <construct the tree of Dimension>
        //TODO
        //endregion <construct the tree of Dimension>

        this.tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(AssertStatement node) {
        // region <grammar>
        /**
         * assert Expression [ : Expression ] ;
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_AssertStatement , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of first expression>
        Expression first_expression = node.getExpression();
        CodeVisitor fExpressionVisitor = new CodeVisitor(tree);
        first_expression.accept(fExpressionVisitor);
        children.add(fExpressionVisitor.getTree());
        //endregion <construct the tree of first expression>

        //region <construct the tree of second expression>
        Expression second_expression = node.getMessage();
        if(second_expression != null){
            //region <construct the tree of semicolon>
            Node semicolonRoot = new Node(NodeType.ADDED_CHAR_SEMICOLON , ":" , id ++);
            CodeStructureTree semicolonTree = new CodeStructureTree(semicolonRoot , ":" , tree);
            children.add(semicolonTree);
            //endregion <construct the tree of semicolon>

            //region <construct the tree of second expression>
            CodeVisitor sExpressionVisitor = new CodeVisitor(tree);
            second_expression.accept(sExpressionVisitor);
            children.add(sExpressionVisitor.getTree());
            //endregion <construct the tree of second expression>
        }
        //endregion <construct the tree of second expression>

        //region <construct the tree of comma>
        Node commaRoot = new Node(NodeType.ADDED_CHAR_COMMA , ";" , id ++);
        CodeStructureTree commaTree = new CodeStructureTree(commaRoot , ";" , tree);
        children.add(commaTree);
        //endregion <construct the tree of comma>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(Assignment node) {
        // region <grammar>
        /**
         * Expression AssignmentOperator Expression
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_Assignment , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of L_expression>
        Expression l_expression = node.getLeftHandSide();
        CodeVisitor l_expressionVisitor = new CodeVisitor(tree);
        l_expression.accept(l_expressionVisitor);
        children.add(l_expressionVisitor.getTree());
        //endregion <construct the tree of L_expression>

        //region <construct  the tree of operator>
        Assignment.Operator operator = node.getOperator();
        Node operatorRoot = new Node(NodeType.CODE_ASSIGNMENT_OPERATOR , operator.toString() , id ++) ;
        CodeStructureTree operatorTree = new CodeStructureTree(operatorRoot , operator.toString() ,tree);
        children.add(operatorTree);
        //endregion <construct  the tree of operator>

        //region <construct the tree of R_expression>
        Expression r_expression = node.getRightHandSide();
        CodeVisitor r_expressionVisitor = new CodeVisitor(tree);
        r_expression.accept(r_expressionVisitor);
        children.add(r_expressionVisitor.getTree());
        //endregion <construct the tree of L_expression>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(Block node) {
        // region <grammar>
        /**
         * { { Statement } }
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_Block , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of {>
        Node lbRoot = new Node(NodeType.ADDED_CHAR_LEFT_BRACE , "{" , id ++);
        CodeStructureTree lbTree = new CodeStructureTree(lbRoot , "{" , tree);
        children.add(lbTree);
        //endregion <construct the tree of }>

        //region <construct the tree of Statement>
        List<Statement> statements = node.statements();
        if(statements != null){
            for(Statement statement : statements){
                CodeVisitor statementVisitor = new CodeVisitor(tree);
                statement.accept(statementVisitor);
                children.add(statementVisitor.getTree());
            }
        }
        //endregion <construct the tree of Statement>

        //region <construct the tree of >
        Node rbRoot = new Node(NodeType.ADDED_CHAR_RIGHT_BRACE , "}" , id ++);
        CodeStructureTree rbTree = new CodeStructureTree(rbRoot , "}" , tree);
        children.add(rbTree);
        //endregion <construct the tree of>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(BlockComment node) {
        return false;
    }

    @Override
    public boolean visit(BooleanLiteral node) {
        // region <grammar>
        /**
         * true
         * false
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_BooleanLiteral , node.toString() , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent );
        //endregion <construct the tree of the root>

        return false;
    }

    @Override
    public boolean visit(BreakStatement node) {
        // region <grammar>
        /**
         * break [ Identifier ] ;
         */
        // endregion <grammar>

        //region <construct the tree of root>
        Node root = new Node(NodeType.CODE_BreakStatement , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of root>

        //region <construct the tree of the root>
        CodeStructureTree breakTree = buildKeyWordTree("break" , tree);
        children.add(breakTree);
        //endregion <construct the tree of the root>

        //region <construct the tree of identifier>
        ASTNode identifier = node.getLabel();
        CodeVisitor identifierVisitor = new CodeVisitor(tree);
        identifier.accept(identifierVisitor);
        children.add(identifierVisitor.getTree());
        //endregion <construct the tree of identifier>

        //region <construct the tree of comma>
        CodeStructureTree commaTree = buildPunctuationTree(";" , NodeType.ADDED_CHAR_COMMA , tree);
        children.add(commaTree);
        //endregion <construct the tree of comma>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(CastExpression node) {
        // region <grammar>
        /**
         * (Type) Expression
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_CastExpression , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of ( >
        CodeStructureTree lpTree = buildPunctuationTree("(" , NodeType.ADDED_CHAR_LEFT_PARENTHESIS , tree);
        children.add(lpTree);
        //endregion <construct the tree of ( >

        //region <construct the tree of Type>
        Type type = node.getType();
        CodeVisitor typeVisitor = new CodeVisitor(tree);
        type.accept(typeVisitor);
        children.add(typeVisitor.getTree());
        //endregion <construct the tree of Type>

        //region <construct the tree of ) >
        CodeStructureTree rpTree = buildPunctuationTree(")" , NodeType.ADDED_CHAR_RIGHT_PARENTHESIS , tree);
        children.add(lpTree);
        //endregion <construct the tree of ) >

        //region <construct the tree of Expression>
        Expression expression = node.getExpression();
        CodeVisitor expressionVisitor = new CodeVisitor(tree);
        expression.accept(expressionVisitor);
        children.add(expressionVisitor.getTree());
        //endregion <construct the tree of Expression>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(CatchClause node) {
        // region <grammar>
        /**
         * catch ( FormalParameter ) Block
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_CatchClause , "" , id ++);
        tree = new CodeStructureTree(root , "" , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of catch>
        CodeStructureTree catchTree = buildKeyWordTree("catch" , tree);
        children.add(catchTree);
        //endregion <construct the tree of catch>

        //region <construct the tree of ( >
        CodeStructureTree lpTree = buildPunctuationTree("(" , NodeType.ADDED_CHAR_LEFT_PARENTHESIS , tree);
        children.add(lpTree);
        //endregion <construct the tree of (>

        //region <construct the tree of FormalParameter>
        SingleVariableDeclaration declaration = node.getException();
        CodeVisitor declarationVisitor = new CodeVisitor(tree);
        declaration.accept(declarationVisitor);
        children.add(declarationVisitor.getTree());
        //endregion <construct the tree of FormalParameter>

        //region <construct the tree of ) >
        CodeStructureTree rpTree = buildPunctuationTree(")" , NodeType.ADDED_CHAR_RIGHT_PARENTHESIS , tree);
        children.add(rpTree);
        //endregion <construct the tree of ) >

        //region <construct the tree of Block>
        Block block = node.getBody();
        CodeVisitor blockVisitor = new CodeVisitor(tree);
        block.accept(blockVisitor);
        children.add(blockVisitor.getTree());
        //endregion <construct the tree of Block>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(CharacterLiteral node) {
        // region <grammar>
        /**
         *
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_CharacterLiteral , node.toString() , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>

        return false;
    }

    @Override
    public boolean visit(ClassInstanceCreation node) {
        // region <grammar>
        /**
         * [ Expression . ] new [ < Type { , Type } > ] Type ( [ Expression { , Expression } ] ) [ AnonymousClassDeclaration ]
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_ClassInstanceCreation , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of Expression>
        Expression expression = node.getExpression();
        if(expression != null){
            CodeVisitor expressionVisitor = new CodeVisitor(tree);
            expression.accept(expressionVisitor);
            children.add(expressionVisitor.getTree());

            Node dotRoot = new Node(NodeType.ADDED_CHAR_DOT , "." , id ++ );
            CodeStructureTree dotTree = new CodeStructureTree(dotRoot , "." , tree);
            children.add(dotTree);
        }
        //endregion <construct the tree of Expression>


        //region <construct the tree of new>
        Node newRoot = new Node(NodeType.ADDED_KEYWORD , "new" , id ++);
        CodeStructureTree newTree = new CodeStructureTree(newRoot , "new" , tree);
        children.add(newTree);
        //endregion <construct the tree of new>

        //region <construct the tree of Type>
        Type type = node.getType();
        CodeVisitor typeVisitor = new CodeVisitor(tree);
        type.accept(typeVisitor);
        children.add(typeVisitor.getTree());
        //endregion <construct the tree of Type>

        //region <construct the tree of (>
        Node lpRoot = new Node(NodeType.ADDED_CHAR_LEFT_PARENTHESIS , "(" , id ++);
        CodeStructureTree lpTree = new CodeStructureTree(lpRoot , "(" , tree);
        children.add(lpTree);
        //endregion <construct the tree of (>

        //region <construct the tree of arguments>
        List<Expression> arguments = node.arguments();
        CodeStructureTree commaTree = null;
        for(Expression argument : arguments){
            if(commaTree != null){
                children.add(commaTree);
                id ++;
            }
            CodeVisitor argumentVisitor = new CodeVisitor(tree);
            argument.accept(argumentVisitor);
            children.add(argumentVisitor.getTree());

            Node commaRoot = new Node(NodeType.ADDED_CHAR_COMMA , "," , id ++);
            commaTree = new CodeStructureTree(commaRoot , "," , tree);
        }
        //endregion <construct the tree of arguments>

        //region <construct the tree of )>
        Node rpRoot = new Node(NodeType.ADDED_CHAR_RIGHT_PARENTHESIS , ")" , id ++);
        CodeStructureTree rpTree = new CodeStructureTree(rpRoot , ")" , tree);
        children.add(rpTree);
        //endregion <construct the tree of )>


        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(CompilationUnit node) {




        // region <grammar>
        /**
         * [ PackageDeclaration ]
         *      { ImportDeclaration }
         *      { TypeDeclaration | EnumDeclaration | AnnotationTypeDeclaration | ; }
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_CompilationUnit , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent );
        //endregion <construct the tree of the root>

        //region <construct the tree of PackageDeclaration>
        PackageDeclaration pDeclaration = node.getPackage();
        CodeVisitor pVisitor = new CodeVisitor(tree);
        pDeclaration.accept(pVisitor);
        children.add(pVisitor.getTree());
        //endregion <construct the tree of PackageDeclaration>

        //region <construct the tree of ImportDeclaration>
        List<ImportDeclaration> iDeclarations = node.imports();
        if(iDeclarations != null && iDeclarations.size() > 0){
            for(ImportDeclaration importDeclaration : iDeclarations){
                CodeVisitor iVisitor = new CodeVisitor(tree);
                importDeclaration.accept(iVisitor);
                children.add(iVisitor.getTree());
            }
        }
        //endregion <construct the tree of ImportDeclaration>

        //region <construct the tree of declarations>
        List<ASTNode> typeDeclarations = node.types();
        for(ASTNode typeDeclaration : typeDeclarations){
            CodeVisitor typeVisitor = new CodeVisitor(tree);
            typeDeclaration.accept(typeVisitor);
            children.add(typeVisitor.getTree());
        }
        //endregion <construct the tree of declarations>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(ConditionalExpression node) {
        // region <grammar>
        /**
         * Expression ? Expression : Expression
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_ConditionalExpression , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of conditionExpression>
        Expression condition = node.getExpression();
        CodeVisitor conditionVisitor = new CodeVisitor(tree);
        condition.accept(conditionVisitor);
        children.add(conditionVisitor.getTree());
        //endregion <construct the tree of conditionExpression>

        //region <construct the tree of ?>
        CodeStructureTree questionTree = buildPunctuationTree("?" , NodeType.ADDED_CHAR_QUESTION, tree);
        children.add(questionTree);
        //endregion <construct the tree of ?>

        //region <construct the tree of thenExpression>
        Expression thenExpression = node.getThenExpression();
        CodeVisitor thenVisitor = new CodeVisitor(tree);
        thenExpression.accept(thenVisitor);
        children.add(thenVisitor.getTree());
        //endregion <construct the tree of thenExpression>

        //region <construct the tree of >
        CodeStructureTree commaTree = buildPunctuationTree(":" , NodeType.ADDED_CHAR_COLON , tree );
        children.add(commaTree);
        //endregion <construct the tree of>

        //region <construct the tree of elseExpression>
        Expression elseExpression = node.getThenExpression();
        CodeVisitor elseVisitor = new CodeVisitor(tree);
        elseExpression.accept(elseVisitor);
        children.add(elseVisitor.getTree());
        //endregion <construct the tree of elseExpression>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(ConstructorInvocation node) {
        return false;
    }

    @Override
    public boolean visit(ContinueStatement node) {
        // region <grammar>
        /**
         * continue [ Identifier ] ;
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_ContinueStatement , "" , id++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of continue>
        CodeStructureTree continueTree = buildKeyWordTree( "continue" , tree);
        children.add(continueTree);
        //endregion <construct the tree of continue>

        //region <construct the tree of identifier>
        SimpleName identifier = node.getLabel();
        if(identifier != null){
            CodeVisitor identifierVisitor = new CodeVisitor(tree);
            identifier.accept(identifierVisitor);;
            children.add(identifierVisitor.getTree());
        }
        //endregion <construct the tree of identifier>

        //region <construct the tree of ;>
        CodeStructureTree semicolonTree = buildPunctuationTree(";" , NodeType.ADDED_CHAR_SEMICOLON , tree);
        children.add(semicolonTree);
        //endregion <construct the tree of ;>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(CreationReference node) {
        return false;
    }

    @Override
    public boolean visit(Dimension node) {
        //region <grammar>
        /**
         * { Annotation } []
         */
        //endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_Dimension , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion

        //region <construct the tree of the Annotation>
        //TODO
        //endregion <construct the tree of the Annotation>

        //region <construct the tree of  [ >
        Node lbRoot = new Node(NodeType.ADDED_CHAR_LEFT_BRACKET , "[" , id ++);
        CodeStructureTree lbTree = new CodeStructureTree(lbRoot , "[" , tree);
        children.add(lbTree);
        //endregion <construct the tree of  [ >

        //region <construct the tree of  ] >
        Node rbRoot = new Node(NodeType.ADDED_CHAR_RIGHT_BRACKET , "]" , id ++);
        CodeStructureTree rbTree = new CodeStructureTree(lbRoot , "]" , tree);
        children.add(rbTree);
        //endregion <construct the tree of  ] >

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(DoStatement node) {
        // region <grammar>
        /**
         * do Statement while ( Expression ) ;
         * */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_DoStatement ,"" , id++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of do>
        CodeStructureTree doTree = buildKeyWordTree("do" , tree);
        children.add(doTree);
        //endregion <construct the tree of do>

        //region <construct the tree of statement>
        Statement statement = node.getBody();
        CodeVisitor statementVisitor = new CodeVisitor(tree);
        statement.accept(statementVisitor);
        children.add(statementVisitor.getTree());
        //endregion <construct the tree of statement>


        //region <construct the tree of while>
        CodeStructureTree whileTree = buildKeyWordTree("while" , tree);
        children.add(whileTree);
        //endregion <construct the tree of while>

        //region <construct the tree of >
        CodeStructureTree lpTree = buildPunctuationTree("(" , NodeType.ADDED_CHAR_LEFT_PARENTHESIS , tree);
        children.add(lpTree);
        //endregion <construct the tree of>

        //region <construct the tree of >
        Expression condition = node.getExpression();
        CodeVisitor conditionVisitor = new CodeVisitor(tree);
        condition.accept(conditionVisitor);
        children.add(conditionVisitor.getTree());
        //endregion <construct the tree of>

        //region <construct the tree of )>
        CodeStructureTree rpTree = buildPunctuationTree(")" , NodeType.ADDED_CHAR_RIGHT_PARENTHESIS , tree);
        children.add(rpTree);
        //endregion <construct the tree of )>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(EmptyStatement node) {
        // region <grammar>
        /**
         * ;
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_EmptyStatement , ";" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>
        return false;
    }

    @Override
    public boolean visit(EnhancedForStatement node) {
        // region <grammar>
        /**
         * for ( FormalParameter : Expression ) Statement
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_EnhancedForStatement , "" , id++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of (>
        CodeStructureTree lpTree = buildPunctuationTree("(" , NodeType.ADDED_CHAR_LEFT_PARENTHESIS , tree);
        children.add(lpTree);
        //endregion <construct the tree of (>

        //region <construct the tree of formalParameter>
        SingleVariableDeclaration parameter = node.getParameter();
        CodeVisitor parameterVisitor = new CodeVisitor(tree);
        parameter.accept(parameterVisitor);
        children.add(parameterVisitor.getTree());
        //endregion <construct the tree of formalParameter>

        //region <construct the tree of :>
        CodeStructureTree colonTree = buildPunctuationTree(":" , NodeType.ADDED_CHAR_COLON , tree);
        children.add(colonTree);
        //endregion <construct the tree of :>


        //region <construct the tree of Expression>
        Expression expression = node.getExpression();
        CodeVisitor expressionVisitor = new CodeVisitor(tree);
        expression.accept(expressionVisitor);
        children.add(expressionVisitor.getTree());
        //endregion <construct the tree of Expression>

        //region <construct the tree of )>
        CodeStructureTree rpTree = buildPunctuationTree(")" , NodeType.ADDED_CHAR_RIGHT_PARENTHESIS , tree);
        children.add(rpTree);
        //endregion <construct the tree of )>

        //region <construct the tree of statement>
        Statement body = node.getBody();
        CodeVisitor bodyVisitor = new CodeVisitor(tree);
        body.accept(bodyVisitor);
        children.add(bodyVisitor.getTree());
        //endregion <construct the tree of statement>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(EnumConstantDeclaration node) {
        return false;
    }

    @Override
    public boolean visit(EnumDeclaration node) {
        return false;
    }

    @Override
    public boolean visit(ExpressionMethodReference node) {
        return false;
    }

    @Override
    public boolean visit(ExpressionStatement node) {
        //grammar: StatementExpression

        Node root = new Node(NodeType.CODE_ExpressionStatement , "" , id ++);
        this.tree = new CodeStructureTree(root , node.toString() , parent);
        children = new ArrayList<Tree>();

        //construct the tree of StatementExpression
        Expression statement = node.getExpression();
        CodeVisitor statementVisitor = new CodeVisitor(tree);
        statement.accept(statementVisitor);
        CodeStructureTree statementTree = statementVisitor.getTree();
        children.add(statementTree);

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(FieldAccess node) {
        // region <grammar>
        /**
         * Expression . Identifier
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_FieldAccess , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of Expression>
        Expression expression = node.getExpression();
        CodeVisitor expressionVisitor = new CodeVisitor(tree);
        expression.accept(expressionVisitor);
        children.add(expressionVisitor.getTree());
        //endregion <construct the tree of Expression>

        //region <construct the tree of .>
        CodeStructureTree dotTree = buildPunctuationTree("." , NodeType.ADDED_CHAR_DOT , tree);
        children.add(dotTree);
        //endregion <construct the tree of .>

        //region <construct the tree of identifier>
        SimpleName identifier = node.getName();
        CodeVisitor identifierVisitor = new CodeVisitor(tree);
        identifier.accept(identifierVisitor);
        children.add(identifierVisitor.getTree());
        //endregion <construct the tree of identifier>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(FieldDeclaration node) {
        return false;
    }

    @Override
    public boolean visit(ForStatement node) {
        // region <grammar>
        /**
         * for ( [ ForInit ] ; [ Expression ] ; [ ForUpdate ] )
         *     Statement
         *
         * ForInit:
         *      Expression { , Expression}
         *
         * ForUpdate:
         *      Expression { , Expression}
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_ForStatement , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of (>
        CodeStructureTree lpTree = buildPunctuationTree("(" , NodeType.ADDED_CHAR_LEFT_PARENTHESIS , tree);
        children.add(lpTree);
        //endregion <construct the tree of (>

        //region <construct the tree of ForInit>
        List<ASTNode> initializers = node.initializers();
        if(initializers != null && initializers.size() > 0) {
            List<CodeStructureTree> initializerTrees = batchProcess(initializers, ",", NodeType.ADDED_CHAR_COMMA, tree);
            children.addAll(initializerTrees);
        }
        //endregion <construct the tree of ForInit>

        //region <construct the tree of ;>
        CodeStructureTree semiColonTree = buildPunctuationTree(";" , NodeType.ADDED_CHAR_SEMICOLON , tree);
        children.add(semiColonTree);
        //endregion <construct the tree of ;>

        //region <construct the tree of Expression>
        Expression expression = node.getExpression();
        CodeVisitor expressionVisitor = new CodeVisitor(tree);
        expression.accept(expressionVisitor);
        children.add(expressionVisitor.getTree());
        //endregion <construct the tree of Expression>

        //region <construct the tree of ;>
        semiColonTree = buildPunctuationTree(";" , NodeType.ADDED_CHAR_SEMICOLON , tree);
        children.add(semiColonTree);
        //endregion <construct the tree of ;>

        //region <construct the tree of ForUpdate>
        List<ASTNode> updaters = node.updaters();
        if(updaters != null && updaters.size() > 0) {
            List<CodeStructureTree> updaterTrees = batchProcess(updaters, ",", NodeType.ADDED_CHAR_COMMA, tree);
            children.addAll(updaterTrees);
        }
        //endregion <construct the tree of ForUpdate>

        //region <construct the tree of )>
        CodeStructureTree rpTree = buildPunctuationTree(")" , NodeType.ADDED_CHAR_RIGHT_PARENTHESIS, tree);
        children.add(rpTree);
        //endregion <construct the tree of )>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(IfStatement node) {
        // region <grammar>
        /**
         * if ( Expression ) Statement [ else Statement]
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_IfStatement , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of if>
        CodeStructureTree ifTree = buildKeyWordTree("if" , tree);
        children.add(ifTree);
        //endregion <construct the tree of if>

        //region <construct the tree of (>
        CodeStructureTree lpTree = buildPunctuationTree("(" , NodeType.ADDED_CHAR_LEFT_PARENTHESIS , tree);
        children.add(lpTree);
        //endregion <construct the tree of (>

        //region <construct the tree of Expression>
        Expression expression = node.getExpression();
        CodeVisitor expressionVisitor = new CodeVisitor(tree);
        expression.accept(expressionVisitor);
        children.add(expressionVisitor.getTree());
        //endregion <construct the tree of Expression>

        //region <construct the tree of )>
        CodeStructureTree rpTree = buildPunctuationTree(")" , NodeType.ADDED_CHAR_RIGHT_PARENTHESIS , tree);
        children.add(rpTree);
        //endregion <construct the tree of )>

        //region <construct the tree of Statement>
        Statement thenStatement = node.getThenStatement();
        CodeVisitor thenStatementVisitor = new CodeVisitor(tree);
        thenStatement.accept(thenStatementVisitor);
        children.add(thenStatementVisitor.getTree());
        //endregion <construct the tree of Statement>

        //region <construct the tree of elseStatement>
        Statement elseStatement = node.getElseStatement();
        if(elseStatement!= null){
            //region <construct the tree of else>
            CodeStructureTree elseTree = buildKeyWordTree("else" , tree);
            children.add(elseTree);
            //endregion <construct the tree of else>

            CodeVisitor elseStatementVisitor = new CodeVisitor(tree);
            elseStatement.accept(elseStatementVisitor);
            children.add(elseStatementVisitor.getTree());
        }
        //endregion <construct the tree of elseStatement>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(ImportDeclaration node) {
        return false;
    }

    @Override
    public boolean visit(InfixExpression node) {
        // region <grammar>
        /**
         * Expression InfixOperator Expression { InfixOperator Expression }
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_InfixExpression , "" , id ++);
        tree = new CodeStructureTree( root , node.toString() , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of >
        List<ASTNode> operands = new ArrayList<>();
        operands.add(node.getLeftOperand());
        operands.add(node.getRightOperand());

        List<Expression> extendedOperands = node.extendedOperands();
        if(extendedOperands != null && extendedOperands.size() > 0)
            operands.addAll(extendedOperands);

        String operator = node.getOperator().toString().trim();
        NodeType operatorType = null;
        //region <get the operator type>
        if(operator.compareTo("+") == 0){
            operatorType = NodeType.CODE_InfixExpression_OPERATOR_TIMES;
        }else if(operator.compareTo("/") == 0){
            operatorType = NodeType.CODE_InfixExpression_OPERATOR_DIVIDE;
        }else if(operator.compareTo("%") == 0){
            operatorType = NodeType.CODE_InfixExpression_OPERATOR_REMAINDER;
        }else if(operator.compareTo("+") == 0){
            operatorType = NodeType.CODE_InfixExpression_OPERATOR_PLUS;
        }else if(operator.compareTo("-") == 0){
            operatorType = NodeType.CODE_InfixExpression_OPERATOR_MINUS;
        }else if(operator.compareTo("<<") == 0){
            operatorType = NodeType.CODE_InfixExpression_OPERATOR_LEFT_SHIFT;
        }else if(operator.compareTo(">>") == 0){
            operatorType = NodeType.CODE_InfixExpression_OPERATOR_RIGHT_SHIFT;
        }else if(operator.compareTo(">>>") == 0){
            operatorType = NodeType.CODE_ASSIGNMENT_OPERATOR_RIGHT_SHIFT_UNSIGNED_ASSIGN;
        }else if(operator.compareTo("<") == 0){
            operatorType = NodeType.CODE_InfixExpression_OPERATOR_LESS;
        }else if(operator.compareTo(">") == 0){
            operatorType = NodeType.CODE_InfixExpression_OPERATOR_GREATER;
        }else if(operator.compareTo("<=") == 0){
            operatorType = NodeType.CODE_InfixExpression_OPERATOR_LESS_EQUALS;
        }else if(operator.compareTo(">=") == 0){
            operatorType = NodeType.CODE_InfixExpression_OPERATOR_GREATER_EQUALS;
        }else if(operator.compareTo("==") == 0){
            operatorType = NodeType.CODE_InfixExpression_OPERATOR_EQUALS;
        }else if(operator.compareTo("!=") == 0){
            operatorType = NodeType.CODE_InfixExpression_OPERATOR_NOT_EQUALS;
        }else if(operator.compareTo("^") == 0){
            operatorType = NodeType.CODE_InfixExpression_OPERATOR_XOR;
        }else if(operator.compareTo("&") == 0){
            operatorType = NodeType.CODE_InfixExpression_OPERATOR_AND;
        }else if(operator.compareTo("|") == 0){
            operatorType = NodeType.CODE_InfixExpression_OPERATOR_OR;
        }else if(operator.compareTo("&&") == 0){
            operatorType = NodeType.CODE_InfixExpression_OPERATOR_AND;
        }else if(operator.compareTo("||") == 0){
            operatorType = NodeType.CODE_InfixExpression_OPERATOR_OR;
        }
        //endregion <get the operator type>


        children.addAll(batchProcess(operands , operator , operatorType , tree));
        //endregion <construct the tree of>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(Initializer node) {
        return false;
    }

    @Override
    public boolean visit(InstanceofExpression node) {
        // region <grammar>
        /**
         * Expression instanceof Type
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_InstanceofExpression , "" , id ++);
        tree = new CodeStructureTree(root , "" , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of Expression>
        Expression expression = node.getLeftOperand();
        CodeVisitor expressionVisitor = new CodeVisitor(tree);
        expression.accept(expressionVisitor);
        children.add(expressionVisitor.getTree());
        //endregion <construct the tree of Expression>

        //region <construct the tree of instanceof>
        CodeStructureTree instanceofTree = buildKeyWordTree("instanceof" , tree);
        children.add(instanceofTree);
        //endregion <construct the tree of instanceof>

        //region <construct the tree of Type>
        Type type = node.getRightOperand();
        CodeVisitor typeVisitor = new CodeVisitor(tree);
        type.accept(typeVisitor);
        children.add(typeVisitor.getTree());
        //endregion <construct the tree of Type>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(IntersectionType node) {
        // region <grammar>
        /**
         * Type & Type { & Type }
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_IntersectionType , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of Types>
        List<ASTNode> types = node.types();
        children.addAll( batchProcess(types , "&" , NodeType.CODE_InfixExpression_OPERATOR_AND , tree));
        //region <construct the tree of Types>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(Javadoc node) {
        // region <grammar>
        //     /**  { TagElement } */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_Javadoc , node.toString() , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>


        return false;
    }

    @Override
    public boolean visit(LabeledStatement node) {
        // region <grammar>
        /**
         * Identifier: Statement
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_LabeledStatement , "" , id ++);
        tree = new CodeStructureTree(root , "" , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of Identifier>
        SimpleName identifier = node.getLabel();
        CodeVisitor identifierVisitor = new CodeVisitor(tree);
        identifier.accept(identifierVisitor);
        children.add(identifierVisitor.getTree());
        //endregion <construct the tree of Identifier>

        //region <construct the tree of :>
        CodeStructureTree colonTree = buildPunctuationTree(":" , NodeType.ADDED_CHAR_COLON , tree);
        children.add(colonTree);
        //endregion <construct the tree of :>

        //region <construct the tree of >
        Statement statement = node.getBody();
        CodeVisitor statementVisitor = new CodeVisitor(tree);
        statement.accept(statementVisitor);
        children.add(statementVisitor.getTree());
        //endregion <construct the tree of>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(LambdaExpression node) {
        return false;
    }

    @Override
    public boolean visit(LineComment node) {
        return false;
    }

    @Override
    public boolean visit(MarkerAnnotation node) {
        return false;
    }

    @Override
    public boolean visit(MemberRef node) {
        return false;
    }

    @Override
    public boolean visit(MemberValuePair node) {
        return false;
    }

    @Override
    public boolean visit(MethodRef node) {
        return false;
    }

    @Override
    public boolean visit(MethodRefParameter node) {
        return false;
    }

    @Override
    public boolean visit(MethodDeclaration node) {

        // region <grammar>
        /**
         * MethodDeclaration:
         * [ Javadoc ] { ExtendedModifier } [ < TypeParameter { , TypeParameter } > ] ( Type | void )
         * Identifier (
         * [ ReceiverParameter , ] [ FormalParameter { , FormalParameter } ]
         * ) { Dimension }
         * [ throws Type { , Type } ]
         * ( Block | ; )
         *
         * ConstructorDeclaration:
         * [ Javadoc ] { ExtendedModifier } [ < TypeParameter { , TypeParameter } > ]
         * Identifier (
         * [ ReceiverParameter , ] [ FormalParameter { , FormalParameter } ]
         * ) { Dimension }
         * [ throws Type { , Type } ]
         * ( Block | ; )

         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_MethodDeclaration , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of Javadoc>
        Javadoc javadoc = node.getJavadoc();
        CodeVisitor javadocVisitor = new CodeVisitor(tree);
        javadoc.accept(javadocVisitor);
        children.add(javadocVisitor.getTree());
        //endregion <construct the tree of Javadoc>

        //region <construct the tree of ExtendedModifier>
        List<Modifier> modifiers = node.modifiers();
        if(modifiers != null && modifiers.size() > 0){
            for(Modifier modifier : modifiers){
                CodeStructureTree t =buildKeyWordTree(modifier.getKeyword().toString() , tree);
                children.add(t);
            }
        }
        //endregion <construct the tree of ExtendedModifier>

        //region <construct the tree of TypeParameter>

            //region <construct the tree of < >
        CodeStructureTree lab = buildPunctuationTree("<" , NodeType.ADDED_CHAR_LEFT_ANGLE_BRACKET , tree);
        children.add(lab);
            //endregion <construct the tree of <>

        List<ASTNode> types = node.typeParameters();
        children.addAll( batchProcess(types , "," , NodeType.ADDED_CHAR_COMMA , tree));

            //region <construct the tree of > >
        CodeStructureTree rab = buildPunctuationTree(">" , NodeType.ADDED_CHAR_RIGHT_ANGLE_BRACKET , tree);
        children.add(rab);
            //endregion <construct the tree of > >

        //endregion <construct the tree of TypeParameter>

        //region <construct the tree of returnType>
        if(!node.isConstructor()) {
            Type type = node.getReturnType2();
            if(type != null) {
                CodeVisitor typeVisitor = new CodeVisitor(tree);
                type.accept(typeVisitor);
                children.add(typeVisitor.getTree());
            }else{
                CodeStructureTree voidTree = buildKeyWordTree("void" , tree);
                children.add(voidTree);
            }
        }
        //endregion <construct the tree of returnType>

        //region <construct the tree of Identifier>
        String methodName = node.getName().toString();
        Node methodNameRoot = new Node(NodeType.CODE_SimpleName , methodName , id++);
        CodeStructureTree methodNameTree = new CodeStructureTree(methodNameRoot , methodName , tree);
        children.add(methodNameTree);
        //endregion <construct the tree of Identifier>

        //region <construct the tree of Parameters>
        CodeStructureTree lpTree = buildPunctuationTree("(" , NodeType.ADDED_CHAR_LEFT_PARENTHESIS , tree);
        children.add(lpTree);

        Type receiverType = node.getReceiverType();
        if(receiverType != null){
            CodeVisitor receiverTypeVisitor = new CodeVisitor(tree);
            receiverType.accept(receiverTypeVisitor);
            children.add(receiverTypeVisitor.getTree());

            CodeStructureTree commaTree = buildPunctuationTree("," , NodeType.ADDED_CHAR_COMMA , tree);
            children.add(commaTree);
        }

        List<ASTNode> formalParameters = node.parameters();
        if(formalParameters != null && formalParameters.size() > 0){
            children.addAll( batchProcess(formalParameters , "," , NodeType.ADDED_CHAR_COMMA , tree));
        }

        CodeStructureTree rpTree = buildPunctuationTree(")" , NodeType.ADDED_CHAR_RIGHT_PARENTHESIS , tree);
        children.add(rpTree);
        //endregion <construct the tree of Parameters>

        //region <construct the tree of Dimension>
        List<Dimension> dimensions = node.extraDimensions();
        if(dimensions != null && dimensions.size() >  0){
            for(Dimension dimension : dimensions){
                CodeVisitor dimensionVisitor = new CodeVisitor(tree);
                dimension.accept(dimensionVisitor);
                children.add(dimensionVisitor.getTree());
            }
        }
        //endregion <construct the tree of Dimension>

        //region <construct the tree of Exceptions>
        List<ASTNode> exceptions = node.thrownExceptionTypes();
        if(exceptions != null && exceptions.size() >0){
            CodeStructureTree throwsTree = buildKeyWordTree("throws" , tree);
            children.add(throwsTree);

            children.addAll( batchProcess( exceptions , "," , NodeType.ADDED_CHAR_COMMA , tree));
        }
        //endregion <construct the tree of Exceptions>

        //region <construct the tree of >
        Block body = node.getBody();
        if(body != null){
            CodeVisitor bodyVisitor = new CodeVisitor(tree);
            body.accept(bodyVisitor);
            children.add(bodyVisitor.getTree());
        }else{
            CodeStructureTree semicolonTree = buildPunctuationTree(";" , NodeType.ADDED_CHAR_SEMICOLON , tree);
            children.add(semicolonTree);
        }
        //endregion <construct the tree of>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(MethodInvocation node) {
        //region <grammar>
        /** original
         *  [ Expression . ] [ < Type { , Type } > ] Identifier ( [ Expression { , Expression } ] )
         */
        /** modify as
         *
         * if Expression exists:
         *    [ Expression . ] [ < Type { , Type } > ] MethodInvocation
         *
         * else:
         *    just as the original one
         *
         */

        //endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_MethodInvocation , "" , id ++);
        this.tree = new CodeStructureTree(root , node.toString() , parent);
        children = new ArrayList<Tree>();
        //endregion <construct the tree of the root>

        //region <construct the tree of the Expression>
        Expression expression = node.getExpression();
        if(expression != null){
            CodeVisitor expressionVisitor = new CodeVisitor(tree);
            expression.accept(expressionVisitor);
            Tree expressionTree = expressionVisitor.getTree();
            children.add(expressionTree);


            Node dotRoot = new Node(NodeType.ADDED_CHAR_DOT , "." , id ++);
            CodeStructureTree dotTree = new CodeStructureTree(dotRoot , "." , tree);
            children.add(dotTree);
        }
        //endregion

        //region <construct the tree of the types>
        List<ASTNode> types = node.typeArguments();
        if(types != null && types.size() > 0){
            //region <construct the tree of <  >
            Node labRoot = new Node(NodeType.ADDED_CHAR_LEFT_ANGLE_BRACKET , "<" , id ++);
            CodeStructureTree labTree = new CodeStructureTree(labRoot , "<" , tree);
            children.add(labTree);
            //endregion <construct the tree of < >

            List<CodeStructureTree> typeTrees = batchProcess(types , "," , NodeType.ADDED_CHAR_COMMA , tree);
            for(CodeStructureTree typeTree : typeTrees){
                children.add(typeTree);
            }

            //region <construct the tree of > >
            Node rabRoot = new Node(NodeType.ADDED_CHAR_RIGHT_ANGLE_BRACKET , ">" , id ++);
            CodeStructureTree rabTree = new CodeStructureTree(labRoot , ">" , tree);
            children.add(rabTree);
            //endregion <construct the tree of > >
        }
        //endregion <construct the tree of the types>

        CodeStructureTree optionalInvocationTree = null;
        List<Tree> optionalChildren ;

        if(expression != null){
            String code = node.toString().replace(expression.toString() + "." , "");

            Node optionalInvocationRoot = new Node(NodeType.CODE_MethodInvocation , "" , id ++);
            optionalInvocationTree = new CodeStructureTree(optionalInvocationRoot , code , tree);
        }


        //region <construct the tree of the Identifier>
        SimpleName name = node.getName();
        Node nameRoot = new Node(NodeType.CODE_SimpleName , name.toString() , id ++);
        CodeStructureTree nameTree = new CodeStructureTree(nameRoot , name.toString() , null);
        //endregion <construct the tree of the Identifier>

        //region <construct the tree of ( >
        Node lpRoot = new Node(NodeType.ADDED_CHAR_LEFT_PARENTHESIS, "(", id++);
        CodeStructureTree lpTree = new CodeStructureTree(lpRoot, "(" , null);
        //endregion <construct the tree of ( >

        //region <construct the tree of the Arguments>
        List<ASTNode> arguments = node.arguments();
        List<CodeStructureTree> argumentTrees = new ArrayList<CodeStructureTree>();
        if (arguments != null) {
            argumentTrees= batchProcess(arguments, ",", NodeType.ADDED_CHAR_COMMA , null);
        }
        //endregion <construct the tree of the Arguments>

        //region <construct the tree of ) >
        Node rpRoot = new Node(NodeType.ADDED_CHAR_LEFT_PARENTHESIS, ")", id++);
        CodeStructureTree rpTree = new CodeStructureTree(rpRoot, ")" , tree);
        //endregion <construct the tree of ) >


        if(expression != null){
            optionalChildren = new ArrayList<Tree>();

            nameTree.setParent(optionalInvocationTree);
            optionalChildren.add(nameTree);

            lpTree.setParent(optionalInvocationTree);
            optionalChildren.add(lpTree);


            for(Tree argumentTree : argumentTrees){
                argumentTree.setParent(optionalInvocationTree);
                optionalChildren.add(argumentTree);
            }

            rpTree.setParent(optionalInvocationTree);
            optionalChildren.add(rpTree);

            optionalInvocationTree.setChildren(optionalChildren);
            children.add(optionalInvocationTree);
        }else{

            nameTree.setParent(tree);
            children.add(nameTree);

            lpTree.setParent(tree);
            children.add(lpTree);

            for(Tree argumentTree : argumentTrees){
                argumentTree.setParent(tree);
                children.add(argumentTree);
            }

            rpTree.setParent(tree);
            children.add(rpTree);
        }

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(Modifier node) {
        // region <grammar>
        /**
         * public | protected | private | static | abstract | final | native | synchronized | transient | volatile | strictfp | default
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_Modifier , node.toString() , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>


        return false;
    }

    @Override
    public boolean visit(NameQualifiedType node) {
        // region <grammar>
        /**
         * Name . { Annotation } SimpleName
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_NameQualifiedType, "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        children = new ArrayList<Tree>();
        //endregion <construct the tree of the root>

        //region <construct the tree of Name>
        Name qualifier = node.getQualifier();
        CodeVisitor qualifierVisitor = new CodeVisitor(tree);
        qualifier.accept(qualifierVisitor);
        children.add(qualifierVisitor.getTree());
        //region <construct the tree of>

        //region <construct the tree of Annotation>
        SimpleName name = node.getName();
        CodeVisitor nameVisitor = new CodeVisitor(tree);
        name.accept(nameVisitor);
        children.add(nameVisitor.getTree());
        //endregion <construct the tree of Annotation>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(NormalAnnotation node) {
        return false;
    }

    @Override
    public boolean visit(NullLiteral node) {

        Node root = new Node(NodeType.CODE_NullLiteral , "null" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);

        return false;
    }

    @Override
    public boolean visit(NumberLiteral node) {
        //region<grammar>
        /**
         *
         */
        //endregion <grammar>

        Node root = new Node(NodeType.CODE_NumberLiteral , node.toString() , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);


        return false;
    }

    @Override
    public boolean visit(PackageDeclaration node) {
        // region <grammar>
        /**
         *  [ Javadoc ] { Annotation } package Name ;
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_PackageDeclaration , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of Javadoc>
        Javadoc javadoc = node.getJavadoc();
        if(javadoc != null){
            CodeVisitor docVisitor = new CodeVisitor(tree);
            javadoc.accept(docVisitor);
            children.add(docVisitor.getTree());
        }
        //endregion <construct the tree of Javadoc>

        //region <construct the tree of Annotation>
        List<Annotation> annotations = node.annotations();
        if(annotations != null && annotations.size() > 0){
            for(Annotation annotation : annotations){
                CodeVisitor annotationVisitor = new CodeVisitor(tree);
                annotation.accept(annotationVisitor);
                children.add(annotationVisitor.getTree());
            }
        }
        //endregion <construct the tree of Annotation>

        //region <construct the tree of package>
        CodeStructureTree packageTree = buildKeyWordTree("package" , tree);
        children.add(packageTree);
        //endregion <construct the tree of package>

        //region <construct the tree of Name>
        Name name = node.getName();
        CodeVisitor nameVisitor = new CodeVisitor(tree);
        name.accept(nameVisitor);
        children.add(nameVisitor.getTree());
        //endregion <construct the tree of Name>

        //region <construct the tree of ;>
        CodeStructureTree semicolonTree = buildPunctuationTree(";" , NodeType.ADDED_CHAR_SEMICOLON , tree);
        children.add(semicolonTree);
        //endregion <construct the tree of ;>

        return false;
    }

    @Override
    public boolean visit(ParameterizedType node) {
        return false;
    }

    @Override
    public boolean visit(ParenthesizedExpression node) {
        // region <grammar>
        /**
         * ( Expression )
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_ParenthesizedExpression , "" , id++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of (>
        CodeStructureTree lpTree = buildPunctuationTree("(" , NodeType.ADDED_CHAR_LEFT_PARENTHESIS , tree);
        children.add(lpTree);
        //endregion <construct the tree of (>

        //region <construct the tree of Expression>
        Expression expression = node.getExpression();
        CodeVisitor expressionVisitor = new CodeVisitor(tree);
        expression.accept(expressionVisitor);
        children.add(expressionVisitor.getTree());
        //endregion <construct the tree of Expression>

        //region <construct the tree of )>
        CodeStructureTree rpTree = buildPunctuationTree(")" , NodeType.ADDED_CHAR_RIGHT_PARENTHESIS , tree);
        children.add(rpTree);
        //endregion <construct the tree of )>

        return false;
    }

    @Override
    public boolean visit(PostfixExpression node) {
        // region <grammar>
        /**
         * Expression PostfixOperator
         *
         * PostfixOperator：
         *  ++ | --
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_PostfixExpression , "" , id++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of Expression>
        Expression expression = node.getOperand();
        CodeVisitor expressionVisitor = new CodeVisitor(tree);
        expression.accept(expressionVisitor);
        children.add(expressionVisitor.getTree());
        //endregion <construct the tree of Expression>

        //region <construct the tree of PostfixOperator>
        Node operatorRoot = new Node(NodeType.CODE_PostfixExpression_Operator , node.getOperator().toString() , id ++);
        CodeStructureTree operatorTree = new CodeStructureTree(operatorRoot , node.getOperator().toString() , tree);
        children.add(operatorTree);
        //endregion <construct the tree of PostfixOperator>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(PrefixExpression node) {
        // region <grammar>
        /**
         * PrefixOperator Expression
         *
         * PrefixOperator:
         *  ++ | -- | + | - | ~ | !
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_PrefixExpression , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of PrefixOperator>
        Node prefixRoot = new Node(NodeType.CODE_PrefixExpression_Operator , node.getOperator().toString() , id ++);
        CodeStructureTree prefixTree = new CodeStructureTree(prefixRoot , node.getOperator().toString() , tree);
        children.add(prefixTree);
        //endregion <construct the tree of PrefixOperator>

        //region <construct the tree of Expression>
        Expression expression = node.getOperand();
        CodeVisitor expressionVisitor = new CodeVisitor(tree);
        expression.accept(expressionVisitor);
        children.add(expressionVisitor.getTree());
        //endregion <construct the tree of Expression>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(PrimitiveType node) {
        //region <grammar>
        /**
         * { Annotation } byte
         * { Annotation } short
         * { Annotation } char
         * { Annotation } int
         * { Annotation } long
         * { Annotation } float
         * { Annotation } double
         * { Annotation } boolean
         * { Annotation } void
         */
        //endregion <grammar>



        //region <construct the tree of Annotation>
        //Todo
        //endregion <construct the tree of Annotation>

        Node root = new Node(NodeType.CODE_PrimitiveType , node.getPrimitiveTypeCode().toString() , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);

        return false;
    }

    @Override
    public boolean visit(QualifiedName node) {
        // region <grammar>
        /**
         * Name . SimpleName
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_QualifiedName , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of Name>
        Name name = node.getQualifier();
        CodeVisitor nameVisitor = new CodeVisitor(tree);
        name.accept(nameVisitor);
        children.add(nameVisitor.getTree());
        //endregion <construct the tree of Name>

        //region<construct the tree of .>
        Node dotRoot = new Node(NodeType.ADDED_CHAR_DOT , "." , id ++);
        CodeStructureTree dotTree = new CodeStructureTree(dotRoot , "." , tree);
        children.add(dotTree);
        //endregion<construct the tree of .>

        //region <construct the tree of SimpleName>
        SimpleName simpleName = node.getName();
        CodeVisitor simpleNameVisitor = new CodeVisitor(tree);
        simpleName.accept(simpleNameVisitor);
        children.add(simpleNameVisitor.getTree());
        //endregion <construct the tree of>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(QualifiedType node) {
        // region <grammar>
        /**
         * Type . {Annotation} SimpleName
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_QualifiedType , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        children = new ArrayList<Tree>();
        //endregion <construct the tree of the root>

        //region <construct the tree of Type>
        Type qualifier = node.getQualifier();
        CodeVisitor qualifierVisitor = new CodeVisitor(tree);
        qualifier.accept(qualifierVisitor);
        children.add(qualifierVisitor.getTree());
        //region <construct the tree of Type>

        //region <construct the tree of Annotation>
        //Todo
        //region <construct the tree of Annotation>

        //region <construct the tree of SimpleName>
        SimpleName name = node.getName();
        CodeVisitor nameVisitor = new CodeVisitor(tree);
        name.accept(nameVisitor);
        children.add(nameVisitor.getTree());
        //region <construct the tree of SimpleName>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(ReturnStatement node) {
        // region <grammar>
        /**
         * return [ Expression ] ;
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_ReturnStatement , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>

        //region <construct the tree of return>
        CodeStructureTree returnTree = buildKeyWordTree("return" , tree);
        children.add(returnTree);
        //endregion <construct the tree of return>

        //region <construct the tree of >
        Expression expression = node.getExpression();
        if(expression != null) {
            CodeVisitor expressionVisitor = new CodeVisitor(tree);
            expression.accept(expressionVisitor);
            children.add(expressionVisitor.getTree());
        }
        //endregion <construct the tree of>

        //region <construct the tree of ;>
        CodeStructureTree semicolonTree = buildPunctuationTree(";" , NodeType.ADDED_CHAR_SEMICOLON , tree);
        children.add(semicolonTree);
        //endregion <construct the tree of ;>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(SimpleName node) {
        // region <grammar>
        /**
         * Identifier
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        String identifier = node.getIdentifier();
        Node root = new Node(NodeType.CODE_SimpleName ,identifier , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of the root>

        return false;
    }

    @Override
    public boolean visit(SimpleType node) {
        // region <grammar>
        /**
         * { Annotation } TypeName
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_SimpleType , "" , id++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        children = new ArrayList<Tree>();

        //endregion <construct the tree of the root>

        //region <construct the tree of Annotation>
        //Todo
        //endregion <construct the tree of Annotation>

        //region <construct the tree of >
        Name name = node.getName();
        CodeVisitor nameVisitor = new CodeVisitor(tree);
        name.accept(nameVisitor);
        children.add(nameVisitor.getTree());
        //endregion <construct the tree of>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(SingleMemberAnnotation node) {
        return false;
    }

    @Override
    public boolean visit(SingleVariableDeclaration node) {
        return false;
    }

    @Override
    public boolean visit(StringLiteral node) {

        Node root = new Node(NodeType.CODE_StringLiteral , node.toString() , id ++);
        tree = new CodeStructureTree(root ,  node.toString() , parent);
        return false;
    }

    @Override
    public boolean visit(SuperConstructorInvocation node) {
        return false;
    }

    @Override
    public boolean visit(SuperFieldAccess node) {
        return false;
    }

    @Override
    public boolean visit(SuperMethodInvocation node) {
        return false;
    }

    @Override
    public boolean visit(SuperMethodReference node) {
        return false;
    }

    @Override
    public boolean visit(SwitchCase node) {
        return false;
    }

    @Override
    public boolean visit(SwitchStatement node) {
        return false;
    }

    @Override
    public boolean visit(SynchronizedStatement node) {
        return false;
    }

    @Override
    public boolean visit(TagElement node) {
        return false;
    }

    @Override
    public boolean visit(TextElement node) {
        return false;
    }

    @Override
    public boolean visit(ThisExpression node) {
        return false;
    }

    @Override
    public boolean visit(ThrowStatement node) {
        return false;
    }

    @Override
    public boolean visit(TryStatement node) {
        return false;
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        return false;
    }

    @Override
    public boolean visit(TypeDeclarationStatement node) {
        return false;
    }

    @Override
    public boolean visit(TypeLiteral node) {
        return false;
    }

    @Override
    public boolean visit(TypeMethodReference node) {
        return false;
    }

    @Override
    public boolean visit(TypeParameter node) {
        return false;
    }

    @Override
    public boolean visit(UnionType node) {
        // region <grammar>
        /**
         *  Type | Type { | Type }
         */
        // endregion <grammar>

        //region <construct the tree of the root>
        Node root = new Node(NodeType.CODE_UnionType , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        children = new ArrayList<Tree>();
        //endregion <construct the tree of the root>

        //region <construct the tree of Types>
        List<Type> types = node.types();
        if(types != null){
            for(Type type : types){
                CodeVisitor typeVisitor = new CodeVisitor(tree);
                type.accept(typeVisitor);
                children.add(typeVisitor.getTree());
            }
        }
        //region <construct the tree of Types>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(VariableDeclarationExpression node) {
        // region <grammar>
        /**
         * { ExtendedModifier } Type VariableDeclarationFragment{ , VariableDeclarationFragment } ;
         */
        // endregion <grammar>

        //region <construct the tree of the root>

        //endregion <construct the tree of the root>

        //region <construct the tree of >
        //endregion <construct the tree of>
        return false;
    }

    @Override
    public boolean visit(VariableDeclarationStatement node) {
        //region <grammar>
        /**
         * { ExtendedModifier } Type VariableDeclarationFragment { , VariableDeclarationFragment } ;
         */
        //endregion <grammar>

        //region <construct the tree of root>
        Node root = new Node(NodeType.CODE_VariableDeclarationStatement , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of root>

        //region <construct the tree of ExtendedModifier>
        List<Modifier> modifiers = node.modifiers();
        if(modifiers != null && modifiers.size() > 0){
            for(Modifier modifier : modifiers){
                Node modifierRoot = new Node(NodeType.CODE_Modifier , modifier.toString() , id ++);
                CodeStructureTree modifierTree = new CodeStructureTree(modifierRoot , modifier.toString() , tree);
                children.add(modifierTree);
            }
        }
        //endregion <construct the tree of ExtendedModifier>

        //region <construct the tree of Type>
        Type type = node.getType();
        CodeVisitor typeVisitor = new CodeVisitor(tree);
        type.accept(typeVisitor);
        children.add(typeVisitor.getTree());
        //endregion <construct the tree of Type>

        //region <construct the tree of VariableDeclarationFragments>
        List<ASTNode> fragments = node.fragments();
        List<CodeStructureTree> fragmentTrees = batchProcess(fragments , "," , NodeType.ADDED_CHAR_COMMA , tree);
        children.addAll(fragmentTrees);
        //endregion <construct the tree of VariableDeclarationFragments>

        //region <construct the tree of ; >
        Node semicolonRoot = new Node(NodeType.ADDED_CHAR_SEMICOLON , ";" , id ++);
        CodeStructureTree semicolonTree = new CodeStructureTree(semicolonRoot , ";" , tree);
        children.add(semicolonTree);
        //endregion <construct the tree of ;>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(VariableDeclarationFragment node) {
        //region <grammar>
        /**
         * Identifier { Dimension } [ = Expression ]
         */
        //endregion <grammar>

        //region <construct the tree of root>
        Node root = new Node(NodeType.CODE_VariableDeclarationFragment , "" , id ++);
        tree = new CodeStructureTree(root , node.toString() , parent);
        //endregion <construct the tree of root>

        //region <construct the tree of Identifier>
        SimpleName identifier = node.getName();
        CodeVisitor identifierVisitor = new CodeVisitor(tree);
        identifier.accept(identifierVisitor);
        children.add(identifierVisitor.getTree());
        //endregion <construct the tree of Identifier>

        //region <construct the tree of Dimension>
        List<Dimension> dimensions = node.extraDimensions();
        if(dimensions != null && dimensions.size() != 0){
            for(Dimension dimension : dimensions){
                CodeVisitor dimensionVisitor = new CodeVisitor(tree);
                dimension.accept(dimensionVisitor);
                children.add(dimensionVisitor.getTree());
            }
        }
        //endregion <construct the tree of Dimension>

        //region <construct the tree of expression>
        Expression expression = node.getInitializer();
        if(expression != null) {
            //region <construct the tree of =>
            Node equalRoot = new Node(NodeType.CODE_InfixExpression_OPERATOR_EQUALS , "=" , id ++);
            CodeStructureTree equalTree = new CodeStructureTree(equalRoot , "=" , tree);
            children.add(equalTree);
            //endregion <construct the tree of =>

            CodeVisitor expressionVisitor = new CodeVisitor(tree);
            expression.accept(expressionVisitor);
            children.add(expressionVisitor.getTree());
        }
        //endregion <construct the tree of expression>

        tree.setChildren(children);
        return false;
    }

    @Override
    public boolean visit(WhileStatement node) {
        return false;
    }

    @Override
    public boolean visit(WildcardType node) {
        return false;
    }

    private CodeStructureTree buildKeyWordTree(String keyWord , CodeStructureTree parentTree){
        Node root = new Node(NodeType.ADDED_KEYWORD , keyWord , id ++);
        CodeStructureTree t = new CodeStructureTree(root , keyWord , parentTree);
        return t;
    }

    private CodeStructureTree buildPunctuationTree(String c , NodeType type , CodeStructureTree parentTree){
        Node root = new Node(type , c , id ++);
        CodeStructureTree t = new CodeStructureTree(root , c , parentTree);
        return t;
    }
}
