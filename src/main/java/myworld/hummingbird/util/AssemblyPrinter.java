package myworld.hummingbird.util;
import myworld.hummingbird.Symbol;
import myworld.hummingbird.TypeFlag;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class AssemblyPrinter {

    public static final String INDENT = "  ";

    private final StringBuilder data;
    private final StringBuilder symbols;
    private final StringBuilder code;

    private int dataLineWidth;

    public AssemblyPrinter(){
        data = new StringBuilder();
        symbols = new StringBuilder();
        code = new StringBuilder();
    }

    private void newline(StringBuilder builder){
        newline(builder, 1);
    }

    private void newline(StringBuilder builder, int indentation){
        builder.append("\n");
        for(var i = 0; i < indentation; i++){
            builder.append(INDENT);
        }
    }

    private void dataBreakIfNeeded(){
        if(dataLineWidth > 80){
            newline(data);
            dataLineWidth = 0;
        }
    }

    private void dataWrite(Runnable r){
        int start = data.length();
        r.run();
        int end = data.length();
        dataLineWidth += (end - start);
        dataBreakIfNeeded();
    }

    public AssemblyPrinter dataInt(int i){
        dataWrite(() -> {
            data.append(i);
            data.append(" ");
        });
        return this;
    }

    public AssemblyPrinter dataLong(long l){
        dataWrite(() -> {
            data.append("l");
            data.append(l);
            data.append(" ");
        });
        return this;
    }

    public AssemblyPrinter dataFloat(float f){
        dataWrite(() -> {
            data.append("f");
            data.append(f);
            data.append(" ");
        });
        return this;
    }

    public AssemblyPrinter dataDouble(double d){
        dataWrite(() -> {
            data.append("d");
            data.append(d);
            data.append(" ");
        });
        return this;
    }

    public AssemblyPrinter dataString(String s){
        dataWrite(() -> {
            data.append("\"");
            data.append(s);
            data.append("\"");
        });
        return this;
    }

    public AssemblyPrinter dataComment(String comment){
        dataWrite(() -> {
            data.append("#");
            data.append(comment);
            newline(data);
            dataLineWidth = 0;
        });
        return this;
    }

    public AssemblyPrinter dataLabel(String label){
        dataWrite(() -> {
            data.append(label);
            data.append(": ");
        });
        return this;
    }

    public AssemblyPrinter symbolComment(String comment){
        newline(symbols);
        symbols.append("#");
        symbols.append(comment);
        return this;
    }

    public AssemblyPrinter addDataSymbol(String name, String label){
        return addSymbol(name, Symbol.Type.DATA, label, null, 0, 0);
    }

    public AssemblyPrinter addFunctionSymbol(String name, String label, TypeFlag rType, int parameters, int registers){
        return addSymbol(name, Symbol.Type.FUNCTION, label, rType, parameters, registers);
    }

    public AssemblyPrinter addForeignSymbol(String name, String label, TypeFlag rType, int parameters){
        return addSymbol(name, Symbol.Type.FOREIGN, label, rType, parameters, 0);
    }

    public AssemblyPrinter addSymbol(String name, Symbol.Type symbolType, String label, TypeFlag rType, int parameters, int registers){

        newline(symbols);

        symbols.append(name);
        symbols.append(" ");
        symbols.append(symbolType.name().toLowerCase());

        if(symbolType == Symbol.Type.DATA || symbolType == Symbol.Type.FUNCTION){
            symbols.append(" $");
            symbols.append(label);
        }

        if(symbolType == Symbol.Type.FUNCTION || symbolType == Symbol.Type.FOREIGN){
            newline(symbols, 2);
            symbols.append(rType.name().toLowerCase());
            newline(symbols, 2);
            symbols.append("parameters ");
            symbols.append(parameters);
            if(symbolType == Symbol.Type.FUNCTION){
                newline(symbols, 2);
                symbols.append("registers ");
                symbols.append(registers);
            }
        }
        newline(symbols);
        return this;
    }

    public AssemblyPrinter codeLabelComment(String label){
        newline(code);
        code.append("#");
        code.append(label);
        return this;
    }

    public AssemblyPrinter codeInsComment(String label){
        newline(code, 2);
        code.append("#");
        code.append(label);
        return this;
    }

    public AssemblyPrinter codeLabel(String label){
        newline(code);
        code.append(label);
        code.append(":");
        return this;
    }

    public AssemblyPrinter codeIns(String ins, String... operands){
        newline(code, 2);
        code.append(ins);
        code.append(" ");
        code.append(String.join(", ", operands));
        return this;
    }

    public AssemblyPrinter withData(AssemblyPrinter other){
        data.append(other.data);
        return this;
    }

    public AssemblyPrinter withSymbols(AssemblyPrinter other){
        symbols.append(other.symbols);
        return this;
    }

    public AssemblyPrinter withCode(AssemblyPrinter other){
        code.append(other.code);
        return this;
    }

    public String build(){
        var builder = new StringBuilder();

        appendSection(builder, ".data", data);
        appendSection(builder, ".symbols", symbols);
        appendSection(builder, ".code", code);

        return builder.toString();
    }

    private void appendSection(StringBuilder builder, String sectionName, StringBuilder contents){
        if(!contents.isEmpty()){
            builder.append(sectionName);
            builder.append("\n");
            builder.append(contents);
            builder.append("\n");
        }
    }

    public static String r(int reg){
        return "r" + reg;
    }

    public static String imm(int i){
        return Integer.toString(i);
    }

    public static String imm(long l){
        return "l" + l;
    }

    public static String imm(float f){
        return "f" + f;
    }

    public static String imm(double d){
        return "d" + d;
    }

    public static String symbol(String symbol){
        return "%" + symbol;
    }

    public static String label(String label){
        return "$" + label;
    }

    public static String hotLoopLabel(String label){
        return "$$" + label;
    }

    public void build(OutputStream os) throws IOException {
        var writer = new OutputStreamWriter(os);

        writer.append(build());
    }

}

