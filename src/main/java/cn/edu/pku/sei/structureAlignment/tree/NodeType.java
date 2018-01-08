package cn.edu.pku.sei.structureAlignment.tree;

/**
 * Created by oliver on 2017/12/23.
 */
public enum NodeType {
    NLTEXT_V,
    CODE_AnnotationTypeDeclaration,
    CODE_AnnotationTypeMemberDeclaration,
    CODE_AnonymousClassDeclaration,
    CODE_ArrayAccess,
    CODE_ArrayCreation,
    CODE_ArrayInitializer,
    CODE_ArrayType,
    CODE_AssertStatement,
    CODE_Assignment,
    CODE_Block,
    CODE_BlockComment,
    CODE_BooleanLiteral, // true | false
    CODE_BreakStatement,
    CODE_CastExpression,
    CODE_CatchClause,
    CODE_CharacterLiteral,
    CODE_ClassInstanceCreation,
    CODE_CompilationUnit,
    CODE_ConditionalExpression,
    CODE_ConstructorInvocation,
    CODE_ContinueStatement,
    CODE_CreationReference,
    CODE_Dimension,
    CODE_DoStatement,
    CODE_EmptyStatement,
    CODE_EnhancedForStatement,
    CODE_EnumConstantDeclaration,
    CODE_EnumDeclaration,
    CODE_ExpressionMethodReference,
    CODE_ExpressionStatement,
    CODE_FieldAccess,
    CODE_FieldDeclaration,
    CODE_ForStatement,
    CODE_IfStatement,
    CODE_ImportDeclaration,
    CODE_InfixExpression,
    CODE_Initializer,
    CODE_InstanceofExpression,
    CODE_IntersectionType,
    CODE_Javadoc,
    CODE_LabeledStatement,
    CODE_LambdaExpression,
    CODE_LineComment,
    CODE_MarkerAnnotation,
    CODE_MemberRef,
    CODE_MemberValuePair,
    CODE_MethodRef,
    CODE_MethodRefParameter,
    CODE_MethodDeclaration,
    CODE_MethodInvocation,
    CODE_Modifier,  //public | protected | private | static | abstract | final | native | synchronized | transient | volatile | strictfp | default
    CODE_NameQualifiedType,
    CODE_NormalAnnotation,
    CODE_NullLiteral,
    CODE_NumberLiteral,
    CODE_PackageDeclaration,
    CODE_ParameterizedType,
    CODE_ParenthesizedExpression,
    CODE_PostfixExpression,
    CODE_PostfixExpression_Operator,
    CODE_PrefixExpression,
    CODE_PrefixExpression_Operator,
    CODE_PrimitiveType,
    CODE_QualifiedName,
    CODE_QualifiedType,
    CODE_ReturnStatement,
    CODE_SimpleName,
    CODE_SimpleType,
    CODE_SingleMemberAnnotation,
    CODE_SingleVariableDeclaration,
    CODE_StringLiteral,
    CODE_SuperConstructorInvocation,
    CODE_SuperFieldAccess,
    CODE_SuperMethodInvocation,
    CODE_SuperMethodReference,
    CODE_SwitchCase,
    CODE_SwitchStatement,
    CODE_SynchronizedStatement,
    CODE_TagElement,
    CODE_TextElement,
    CODE_ThisExpression,
    CODE_ThrowStatement,
    CODE_TryStatement,
    CODE_TypeDeclaration,
    CODE_TypeDeclarationStatement,
    CODE_TypeLiteral,
    CODE_TypeMethodReference,
    CODE_TypeParameter,
    CODE_UnionType,
    CODE_VariableDeclarationExpression,
    CODE_VariableDeclarationStatement,
    CODE_VariableDeclarationFragment,
    CODE_WhileStatement,
    CODE_WildcardType,

    // all the type below are used to indicate the nodes are left node.

    CODE_Identifier,

    CODE_InfixExpression_OPERATOR_TIMES,        // *
    CODE_InfixExpression_OPERATOR_DIVIDE,       // /
    CODE_InfixExpression_OPERATOR_REMAINDER,    // %
    CODE_InfixExpression_OPERATOR_PLUS,         // +
    CODE_InfixExpression_OPERATOR_MINUS,        // -
    CODE_InfixExpression_OPERATOR_LEFT_SHIFT,   // <<
    CODE_InfixExpression_OPERATOR_RIGHT_SHIFT,  // >>
    CODE_InfixExpression_OPERATOR_RIGHT_SHIFT_UNSIGNED,     // >>>
    CODE_InfixExpression_OPERATOR_LESS,         // <
    CODE_InfixExpression_OPERATOR_GREATER,      // >
    CODE_InfixExpression_OPERATOR_LESS_EQUALS,  // <=
    CODE_InfixExpression_OPERATOR_GREATER_EQUALS,   // >=
    CODE_InfixExpression_OPERATOR_EQUALS,       // ==
    CODE_InfixExpression_OPERATOR_NOT_EQUALS,   // !=
    CODE_InfixExpression_OPERATOR_XOR,           // ^
    CODE_InfixExpression_OPERATOR_AND,           // &
    CODE_InfixExpression_OPERATOR_OR,            // |
    CODE_InfixExpression_OPERATOR_CONDITIONAL_AND,           // &&
    CODE_InfixExpression_OPERATOR_CONDITIONAL_OR,           // ||

    CODE_ASSIGNMENT_OPERATOR,
    CODE_ASSIGNMENT_OPERATOR_ASSIGN,    // =
    CODE_ASSIGNMENT_OPERATOR_PLUS_ASSIGN,   // +=
    CODE_ASSIGNMENT_OPERATOR_MINUS_ASSIGN,  // -=
    CODE_ASSIGNMENT_OPERATOR_TIMES_ASSIGN,  //*=
    CODE_ASSIGNMENT_OPERATOR_DIVIDE_ASSIGN,     // /=
    CODE_ASSIGNMENT_OPERATOR_BIT_AND_ASSIGN,    // &=
    CODE_ASSIGNMENT_OPERATOR_BIT_OR_ASSIGN,     // |=
    CODE_ASSIGNMENT_OPERATOR_BIT_XOR_ASSIGN,    // ^=
    CODE_ASSIGNMENT_OPERATOR_REMAINDER_ASSIGN,      // %=
    CODE_ASSIGNMENT_OPERATOR_LEFT_SHIFT_ASSIGN,     // <<=
    CODE_ASSIGNMENT_OPERATOR_RIGHT_SHIFT_SIGNED_ASSIGN,     // >>=
    CODE_ASSIGNMENT_OPERATOR_RIGHT_SHIFT_UNSIGNED_ASSIGN,   // >>>=



    ADDED_MethodInvocation_Types,
    ADDED_MethodInvocation_Arguments,
    ADDED_KEYWORD,
    ADDED_OPERATOR,
    ADDED_CHAR_LEFT_PARENTHESIS,    // (
    ADDED_CHAR_RIGHT_PARENTHESIS,   // )
    ADDED_CHAR_LEFT_BRACKET,        // [
    ADDED_CHAR_RIGHT_BRACKET,       // ]
    ADDED_CHAR_LEFT_BRACE,          // {
    ADDED_CHAR_RIGHT_BRACE,         // }
    ADDED_CHAR_LEFT_ANGLE_BRACKET,  // <
    ADDED_CHAR_RIGHT_ANGLE_BRACKET, // >
    ADDED_CHAR_COLON,               // :
    ADDED_CHAR_COMMA,                // ,
    ADDED_CHAR_SEMICOLON,             // ;
    ADDED_CHAR_DOT,                 // .
    ADDED_CHAR_QUESTION,            //?
    NULL
}
