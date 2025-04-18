package myworld.hummingbird.assembler;

import myworld.hummingbird.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Assembler {

    public static final String DATA_SECTION = ".data";
    public static final String SYMBOL_SECTION = ".symbols";
    public static final String CODE_SECTION = ".code";


    protected final Pattern comment;
    protected final Pattern whitespace;
    protected final Pattern newline;
    protected final Pattern sectionName;
    protected final Pattern labelDef;
    protected final Pattern labelUse;
    protected final Pattern symbolName;
    protected final Pattern symbolUse;
    protected final Pattern register;
    protected final Pattern instruction;
    protected final Pattern intLiteral;
    protected final Pattern floatLiteral;
    protected final Pattern stringLiteral;

    public final Map<String, Method> opcodeFactories;

    public Assembler(){
        opcodeFactories = new HashMap<>();
        register(Opcodes.class);

        comment = Pattern.compile("#.*");
        whitespace = Pattern.compile("\\h+");
        newline = Pattern.compile("\n|\r\n|\r");
        sectionName = Pattern.compile("\\.\\w+");
        labelDef = Pattern.compile("[\\S&&[^:,#]]+:");
        labelUse = Pattern.compile("\\$(\\$)?[\\S&&[^:,#]]+");
        symbolName = Pattern.compile("[\\S&&[^:,#]]+");
        symbolUse = Pattern.compile("%[\\S&&[^:,#]]+");
        register = Pattern.compile("[rp]\\d+");
        instruction = Pattern.compile("\\w+");
        intLiteral = Pattern.compile("[IiLl]?(0x|0b|0o)?-?\\d+");
        floatLiteral = Pattern.compile("[FfDd]-?\\d*((\\.)?\\d+)?([eE]-?\\d+)?");
        stringLiteral = Pattern.compile("\"(\\\\\"|[^\"])*\"");
    }

    public void register(Class<?> opcodes){
        for(var method : opcodes.getMethods()){
            if(method.isAnnotationPresent(Assembles.class)){
                var annotation = method.getAnnotation(Assembles.class);
                var opName = annotation.value().toUpperCase();
                if(opcodeFactories.containsKey(opName)){
                    throw new IllegalStateException("Assembler for opcode " + opName + " has already been registered");
                }
                if(!Modifier.isStatic(method.getModifiers())){
                    throw new IllegalArgumentException("Assembler for opcode " + opName + " is not static");
                }
                opcodeFactories.put(opName, method);
            }
        }
    }

    public Executable assemble(CharSequence source) throws AssemblyException {
        var builder = Executable.builder();
        var labels = new Labels();
        var asm = new CharStream(source);

        try {
            while (asm.hasRemaining()) {
                var section = getSectionName(asm);
                if (section == null) {
                    throw new AssemblyException("Invalid assembly: could not read section at " + asm.debug(15));
                }

                if (DATA_SECTION.equals(section)) {
                    parseDataSection(asm, labels, builder);
                } else if (SYMBOL_SECTION.equals(section)) {
                    parseSymbolSection(asm, labels, builder);
                } else if (CODE_SECTION.equals(section)) {
                    parseCodeSection(asm, labels, builder);
                } else {
                    throw new AssemblyException("Invalid section name: " + section);
                }

            }

            resolveLabels(labels, builder);
        } catch (Exception e) {
            throw new AssemblyException("Assembly failed", e);
        }

        return builder.build();
    }

    protected void parseDataSection(CharStream asm, Labels labels, Executable.Builder builder) throws AssemblyException {
        skipNewlinesAndComments(asm);
        var encoder = new DataBufferEncoder();
        // Parse until we hit the next section start
        while(moreWithinSection(asm)){

            var label = consumeLabelDef(asm);
            if(label != null){
                labels.markResolved(label, encoder.indexOfNextWrite());
            }

            encoder.write(parseLiteral(asm));

            skipNewlinesAndComments(asm);
        }

        builder.appendData(encoder.toArray());

    }

    protected void parseSymbolSection(CharStream asm, Labels labels, Executable.Builder builder) throws AssemblyException {
        skipNewlinesAndComments(asm);

        var foreignIndex = 0;

        while(moreWithinSection(asm)){
            var name = consume(asm, symbolName);
            syntaxCheck(asm, name, "symbol name");
            var nameStr = name.toString();
            skipNewlinesAndComments(asm);

            var typeName = consume(asm, symbolName);
            syntaxCheck(asm, typeName, "symbol type");
            skipNewlinesAndComments(asm);

            var type = Symbol.Type.valueOf(typeName.toString().toUpperCase());
            if(type == Symbol.Type.DATA){
                var label = parseLabelUse(asm);
                var index = builder.indexOfNextSymbol();
                builder.appendSymbol(null);
                labels.markUnresolvedUse(label, (resolvedLabel, resolvedIndex) -> {
                    builder.replaceSymbol(index, Symbol.data(nameStr, resolvedIndex));
                });
            }else if(type == Symbol.Type.FUNCTION){
                var label = parseLabelUse(asm);
                skipNewlinesAndComments(asm);

                var rType = parseReturnTypeFlag(asm);
                skipNewlinesAndComments(asm);

                var paramCounts = parseParamCounts(asm, "parameters");
                skipNewlinesAndComments(asm);

                var registerCounts = parseRegisterCount(asm);
                skipNewlinesAndComments(asm);

                var index = builder.indexOfNextSymbol();
                builder.appendSymbol(Symbol.empty(nameStr));

                labels.markUnresolvedUse(label, (resolvedLabel, resolvedIndex) -> {
                    builder.replaceSymbol(index, Symbol.function(nameStr, resolvedIndex, rType, paramCounts, registerCounts));
                });
            }else if(type == Symbol.Type.FOREIGN){

                var rType = parseReturnTypeFlag(asm);
                skipNewlinesAndComments(asm);

                var paramCounts = parseParamCounts(asm, "parameters");
                skipNewlinesAndComments(asm);

                builder.appendSymbol(Symbol.foreignFunction(nameStr, foreignIndex, rType, paramCounts));
                foreignIndex++;
            }
            skipNewlinesAndComments(asm);

        }

    }

    protected void parseCodeSection(CharStream asm, Labels labels, Executable.Builder builder) throws AssemblyException {
        skipNewlinesAndComments(asm);
        while(moreWithinSection(asm)){

            skipNewlinesAndComments(asm);

            // Check for label
            var labelDef = consumeLabelDef(asm);
            if(labelDef != null){
                labels.markResolved(labelDef, builder.indexOfNextOpcode());
            }

            skipNewlinesAndComments(asm);

            var ins = consume(asm, instruction);
            syntaxCheck(asm, ins, "instruction");
            var opName = ins.toString().toUpperCase();

            var operands = new ArrayList<>();
            var pending = false;
            var unresolvedLabels = new ArrayList<Label>();
            skipWhitespace(asm);
            while(!asm.peek(newline, comment) && asm.peek() != CharStream.END){

                if(asm.peek() == '$'){
                    var label = parseLabelUse(asm);
                    if(labels.isResolved(label)){
                        operands.add(labels.getResolvedIndex(label));
                    }else{
                        operands.add(label);
                        unresolvedLabels.add(label);
                        pending = true;
                    }
                }else if(asm.peek() == '%'){
                    var symbol = parseSymbolUse(asm);
                    var index = builder.symbolIndex(symbol);
                    if(index == -1){
                        throw new AssemblyException("Symbol does not exist: " + symbol);
                    }
                    operands.add(index);
                }else{
                    operands.add(parseOperand(asm));
                }

                skipWhitespace(asm);

                if(asm.peek() == ','){
                    asm.advance(1);
                }
                skipWhitespace(asm);
            }

            if(pending){
                var index = builder.appendOpcode(null);
                var pendingOpcode = new PendingOpcode(index, opName, operands);
                for(var label : unresolvedLabels){
                    labels.markUnresolvedUse(label, (resolved, resolvedIndex) -> {
                        for(int i = 0; i < pendingOpcode.operands().size(); i++){
                            if(pendingOpcode.operands().get(i) instanceof Label l){
                                pendingOpcode.operands().set(i, labels.getResolvedIndex(l));
                            }
                        }
                        builder.replaceOpcode(pendingOpcode.index(), makeOpcode(pendingOpcode.name(), pendingOpcode.operands()));
                    });
                }
            }else{
                builder.appendOpcode(makeOpcode(opName, operands));
            }

            skipNewlinesAndComments(asm);
        }
    }

    protected void resolveLabels(Labels labels, Executable.Builder builder) throws AssemblyException {
        // Iterate over all unresolved labels, replacing their placeholder opcodes
        // with opcodes where the labels have been filled in
        for(var entry : labels.getUnresolvedLabelUses().entrySet()){
            var label = entry.getKey();
            var uses = entry.getValue();

            if(!labels.isResolved(label)){
                throw new AssemblyException("Unresolved label: " + label);
            }

            for(var user : uses){
                user.resolved(label.name(), labels.getResolvedIndex(label));
            }
        }
    }

    protected void skipNewlinesAndComments(CharStream asm){
        boolean consumeMore = true;
        while(consumeMore){
            consumeMore = consume(asm, whitespace) != null
                    || consume(asm, comment) != null
                    || consume(asm, newline) != null;
        }
    }

    protected void skipWhitespace(CharStream asm){
        boolean consumeMore = true;
        while(consumeMore){
            consumeMore = consume(asm, whitespace) != null;
        }
    }

    protected Object parseLiteral(CharStream asm) throws AssemblyException {
        var sequence = consume(asm, intLiteral);
        if(sequence != null) return parseIntLiteral(sequence);
        sequence = consume(asm, floatLiteral);
        if(sequence != null) return parseFloatLiteral(sequence);
        sequence = consume(asm, stringLiteral);
        if(sequence != null) return parseStringLiteral(sequence);
        throw new AssemblyException("Invalid literal: " + asm.debug(10));
    }

    protected Label parseLabelUse(CharStream asm) throws AssemblyException {
        var sequence = consume(asm, labelUse);
        // Trim leading '$' or '$'
        if(sequence != null) {
            var isHotJump = sequence.charAt(1) == '$';
            return new Label(sequence.subSequence(isHotJump ? 2 : 1, sequence.length()).toString(), isHotJump);
        }
        throw new AssemblyException("Not a label use: " + asm.debug(10));
    }

    protected String parseSymbolUse(CharStream asm) throws AssemblyException {
        var sequence = consume(asm, symbolUse);
        // Trim leading '%'
        if(sequence != null) return sequence.subSequence(1, sequence.length()).toString();
        throw new AssemblyException("Not a symbol use: " + asm.debug(10));
    }

    protected int parseRegister(CharStream asm) throws AssemblyException {
        try{
            var sequence = consume(asm, register);
            if(sequence != null) {
                return switch (sequence.charAt(0)){
                    // Drop leading 'r' or 'p' and parse reference.
                    case 'r' -> Integer.parseInt(sequence.subSequence(1, sequence.length()).toString());
                    case 'p' -> -(Integer.parseInt(sequence.subSequence(1, sequence.length()).toString()) + Fiber.CALL_FRAME_SAVED_REGISTERS + 1);
                    default -> throw new AssemblyException("Invalid register reference: " + asm.debug(10));
                };
            }
        }catch (Exception e){
            throw new AssemblyException("Invalid register reference: " + asm.debug(10));
        }
        throw new AssemblyException("Invalid register reference: " + asm.debug(10));
    }

    protected Object parseOperand(CharStream asm) throws AssemblyException {
        if(asm.peek(register)){
            return parseRegister(asm);
        }else{
            return parseLiteral(asm);
        }
    }

    protected Number parseIntLiteral(CharSequence sequence) throws AssemblyException {
        try{
            var signifier = sequence.charAt(0);
            var hasSignifier = Character.isAlphabetic(signifier);

            String str;
            if(hasSignifier){
                str = sequence.subSequence(1, sequence.length() - 1).toString();
            }else{
                str = sequence.toString();
            }

            var radix = getRadix(str);
            if(radix != 10){
                str = str.substring(2);
            }

            if(hasSignifier && Character.toUpperCase(signifier) == 'L'){
                return Long.parseLong(str, radix);
            }else{
                return Integer.parseInt(str, radix);
            }
        }
        catch(Exception ex){
            throw new AssemblyException("Invalid int literal: " + sequence.toString());
        }
    }

    protected int getRadix(String intLiteral){
        if(intLiteral.startsWith("0x")){
            return 16;
        }else if(intLiteral.startsWith("0o")){
            return 8;
        }else if(intLiteral.startsWith("0b")){
            return 2;
        }else{
            return 10;
        }
    }

    protected Object parseFloatLiteral(CharSequence sequence) throws AssemblyException {
        try{
            var signifier = sequence.charAt(0);
            var hasSignifier = Character.isAlphabetic(signifier);

            String str;
            if(hasSignifier){
                str = sequence.subSequence(1, sequence.length()).toString();
            }else{
                str = sequence.toString();
            }

            if(hasSignifier && Character.toUpperCase(signifier) == 'D'){
                return Double.parseDouble(str);
            }else{
                return Float.parseFloat(str);
            }
        }
        catch(Exception ex){
            throw new AssemblyException("Invalid float literal: " + sequence.toString(), ex);
        }
    }

    protected TypeFlag parseReturnTypeFlag(CharStream asm) throws AssemblyException {
        var type = consume(asm, symbolName);
        syntaxCheck(asm, type, "symbol return type");

        return TypeFlag.valueOf(type.toString().toUpperCase());
    }

    protected int parseParamCounts(CharStream asm, String requiredName) throws AssemblyException {
        var name = consume(asm, symbolName);
        syntaxCheck(asm, name, "symbol type count");

        var nameStr = name.toString();
        if(!nameStr.equals(requiredName)){
            throw new AssemblyException("Wrong type count name: expected " + requiredName + ", got " + nameStr);
        }

        skipWhitespace(asm);

        return parseIntLiteral(consume(asm, intLiteral)).intValue();
    }

    protected int parseRegisterCount(CharStream asm) throws AssemblyException {
        var name = consume(asm, symbolName);
        syntaxCheck(asm, name, "symbol register count");

        if(!name.equals("registers")){
            throw new AssemblyException("Wrong register count name: expected registers, got " + name);
        }

        skipWhitespace(asm);

        return parseIntLiteral(consume(asm, intLiteral)).intValue();
    }

    protected Object parseStringLiteral(CharSequence sequence){
        return sequence.subSequence(1, sequence.length() - 2).toString();
    }

    protected String consumeLabelDef(CharStream asm){
        var sequence = consume(asm, labelDef);
        if(sequence == null) {
            return null;
        }
        return sequence.subSequence(0, sequence.length() - 1).toString(); // Trim trailing colon
    }

    protected String getSectionName(CharStream asm){
        skipNewlinesAndComments(asm);
        var name = consume(asm, sectionName);
        return name != null ? name.toString() : null;
    }

    protected CharSequence consume(CharStream asm, Pattern p){
        var matcher = p.matcher(asm.source());
        matcher.region(asm.offset(), asm.source().length());
        if(matcher.lookingAt()){
            var length = matcher.end() - matcher.start();
            var result = asm.slice(asm.offset(), length);
            asm.advance(length);
            return result;
        }
        return null;
    }

    protected boolean moreWithinSection(CharStream asm){
        return asm.peek() != '.' && asm.peek() != CharStream.END;
    }

    protected Opcode makeOpcode(String name, List<Object> operands) throws AssemblyException {

        var factory = opcodeFactories.get(name.toUpperCase());
        if(factory == null){
            throw new AssemblyException("Unrecognized instruction: " + name);
        }

        if(operands.size() != factory.getParameterCount()){
            throw new AssemblyException(name + " operand count mismatch: Expected " + factory.getParameterCount() + " got " + operands.size());
        }

        for(int i = 0; i < operands.size(); i++){
            var pType = factory.getParameterTypes()[i];
            var type = operands.get(i).getClass();
            if(!pType.isAssignableFrom(type)){
                throw new AssemblyException(name + " operand " + i + " type mismatch: Expected " + pType.getName() + " got " + type.getName());
            }
        }

        try {
            return (Opcode) factory.invoke(null, operands.toArray(Object[]::new));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new AssemblyException("Could not create operand " + name, e);
        }
    }

    protected void syntaxCheck(CharStream asm, CharSequence check, String typeName) throws AssemblyException {
        if(check == null){
            throw new AssemblyException("Syntax error: could not match " + typeName + ": " + asm.debug(15));
        }
    }

    protected class CharStream {

        public static final char END = '\0';

        protected final CharSequence source;
        protected int offset;

        public CharStream(CharSequence source){
            this.source = source;
            offset = 0;
        }

        public CharSequence source(){
            return source;
        }

        public int offset(){
            return offset;
        }

        public int advance(int places){
            offset += places;
            return offset;
        }

        public char peek(){
            if(!hasRemaining()){
                return END;
            }
            return source.charAt(offset);
        }

        public boolean peek(Pattern... patterns){
            for(var pattern : patterns){
                if(pattern.matcher(source.subSequence(offset, source.length())).lookingAt()){
                    return true;
                }
            }
            return false;
        }

        public CharSequence slice(int start, int length){
            return source.subSequence(start, start + length);
        }

        public CharSequence safeSlice(int start, int length){
            var end = start + length;
            if(end >= source.length()){
                end = source.length() - 1;
            }
            return source.subSequence(start, end);
        }

        public boolean hasRemaining(){
            return offset < source.length();
        }

        public String debug(int length){
            return safeSlice(offset, length).toString() + "...";
        }

    }

}
