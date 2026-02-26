package com.example.decompi.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java_cup.runtime.*;

%%

%public
%class Lexer
%cup
%line
%column
%unicode

%{
    private Symbol symbol(int type) {
        return new Symbol(type, yyline + 1, yycolumn + 1);
    }
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline + 1, yycolumn + 1, value);
    }
%}

// Definitions
LineTerminator = \r|\n|\r\n
WhiteSpace = {LineTerminator} | [ \t\f]
Comment = "#" [^\r\n]*
Integer = [0-9]+
Decimal = [0-9]+ "." [0-9]* | "." [0-9]+
Number = {Integer} | {Decimal}
Identifier = [a-zA-Z][a-zA-Z0-9_]*
StringLiteral = \" ([^\"\\] | \\.)* \"
HexColor = "H" [0-9a-fA-F]{6}

%%

<YYINITIAL> {
    // Keywords Pseudocode
    "VAR"               { return symbol(sym.VAR); }
    "SI"                { return symbol(sym.SI); }
    "ENTONCES"          { return symbol(sym.ENTONCES); }
    "FIN" {WhiteSpace}+ "SI"  { return symbol(sym.FINSI); }
    "FINSI"             { return symbol(sym.FINSI); }
    "MIENTRAS"          { return symbol(sym.MIENTRAS); }
    "HACER"             { return symbol(sym.HACER); }
    "FIN" {WhiteSpace}+ "MIENTRAS" { return symbol(sym.FINMIENTRAS); }
    "FINMIENTRAS"       { return symbol(sym.FINMIENTRAS); }
    "MOSTRAR"           { return symbol(sym.MOSTRAR); }
    "LEER"              { return symbol(sym.LEER); }
    "INICIO"            { return symbol(sym.INICIO); }
    "FIN"               { return symbol(sym.FIN); }

    // Operators
    "+"                 { return symbol(sym.PLUS); }
    "-"                 { return symbol(sym.MINUS); }
    "*"                 { return symbol(sym.MULT); }
    "/"                 { return symbol(sym.DIV); }
    "=="                { return symbol(sym.EQUALS); }
    "!="                { return symbol(sym.NOT_EQUALS); }
    ">="                { return symbol(sym.GREATER_EQUAL); }
    "<="                { return symbol(sym.LESS_EQUAL); }
    ">"                 { return symbol(sym.GREATER); }
    "<"                 { return symbol(sym.LESS); }
    "&&"                { return symbol(sym.AND); }
    "||"                { return symbol(sym.OR); }
    "!"                 { return symbol(sym.NOT); }
    "="                 { return symbol(sym.ASSIGN); }

    // Symbols
    "("                 { return symbol(sym.LPAREN); }
    ")"                 { return symbol(sym.RPAREN); }
    ","                 { return symbol(sym.COMMA); }
    "|"                 { return symbol(sym.PIPE); }
    "%%%%"              { return symbol(sym.SEPARATOR); }

    // Config Keywords
    "%DEFAULT"               { return symbol(sym.CONF_DEFAULT); }
    "%COLOR_TEXTO_SI"        { return symbol(sym.CONF_COLOR_TEXTO_SI); }
    "%COLOR_SI"              { return symbol(sym.CONF_COLOR_SI); }
    "%FIGURA_SI"             { return symbol(sym.CONF_FIGURA_SI); }
    "%LETRA_SI"              { return symbol(sym.CONF_LETRA_SI); }
    "%LETRA_SIZE_SI"         { return symbol(sym.CONF_LETRA_SIZE_SI); }
    "%COLOR_TEXTO_MIENTRAS"  { return symbol(sym.CONF_COLOR_TEXTO_MIENTRAS); }
    "%COLOR_MIENTRAS"        { return symbol(sym.CONF_COLOR_MIENTRAS); }
    "%FIGURA_MIENTRAS"       { return symbol(sym.CONF_FIGURA_MIENTRAS); }
    "%LETRA_MIENTRAS"        { return symbol(sym.CONF_LETRA_MIENTRAS); }
    "%LETRA_SIZE_MIENTRAS"   { return symbol(sym.CONF_LETRA_SIZE_MIENTRAS); }
    "%COLOR_TEXTO_BLOQUE"    { return symbol(sym.CONF_COLOR_TEXTO_BLOQUE); }
    "%COLOR_BLOQUE"          { return symbol(sym.CONF_COLOR_BLOQUE); }
    "%FIGURA_BLOQUE"         { return symbol(sym.CONF_FIGURA_BLOQUE); }
    "%LETRA_BLOQUE"          { return symbol(sym.CONF_LETRA_BLOQUE); }
    "%LETRA_SIZE_BLOQUE"     { return symbol(sym.CONF_LETRA_SIZE_BLOQUE); }

    // Figures
    "ELIPSE"                { return symbol(sym.FIG_ELIPSE); }
    "CIRCULO"               { return symbol(sym.FIG_CIRCULO); }
    "PARALELOGRAMO"         { return symbol(sym.FIG_PARALELOGRAMO); }
    "RECTANGULO"            { return symbol(sym.FIG_RECTANGULO); }
    "ROMBO"                 { return symbol(sym.FIG_ROMBO); }
    "RECTANGULO_REDONDEADO" { return symbol(sym.FIG_RECTANGULO_REDONDEADO); }

    // Fonts
    "ARIAL"                 { return symbol(sym.FONT_ARIAL); }
    "TIMES_NEW_ROMAN"       { return symbol(sym.FONT_TIMES_NEW_ROMAN); }
    "COMIC_SANS"            { return symbol(sym.FONT_COMIC_SANS); }
    "VERDANA"               { return symbol(sym.FONT_VERDANA); }

    // Literals
    {Number}            { return symbol(sym.NUMBER, Double.parseDouble(yytext())); }
    {HexColor}          { return symbol(sym.HEX_COLOR, yytext()); }
    {StringLiteral}     { return symbol(sym.STRING, yytext().substring(1, yytext().length()-1)); }
    {Identifier}        { return symbol(sym.ID, yytext()); }

    {WhiteSpace}        { /* ignore */ }
    {Comment}           { /* ignore */ }
}

[^] {
    // Return error token or handle lexical error
    System.err.println("Lexical error: " + yytext() + " at line " + (yyline+1) + ", column " + (yycolumn+1));
}
