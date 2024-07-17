package myworld.hummingbird.test;

import myworld.hummingbird.HummingbirdVM;
import myworld.hummingbird.assembler.Assembler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

public class TestPrograms {

    public final HummingbirdVM countOneMillion = load("myworld/hummingbird/test/programs/countOneMillion.hasm");
    public final HummingbirdVM callOneMillion = load("myworld/hummingbird/test/programs/callOneMillion.hasm");
    public final HummingbirdVM fibonacci30 = load("myworld/hummingbird/test/programs/fibonacci30.hasm");
    public final HummingbirdVM goldenRatio = load("myworld/hummingbird/test/programs/goldenRatio.hasm");
    public final HummingbirdVM simpleFunction = load("myworld/hummingbird/test/programs/simpleFunction.hasm");
    public final HummingbirdVM simpleFiber = load("myworld/hummingbird/test/programs/simpleFiber.hasm");
    public final HummingbirdVM recursiveCountOneMillion = load("myworld/hummingbird/test/programs/recursiveCountOneMillion.hasm");

    public Map<String, Callable<Object>> javaTestPrograms(){
        var programs = new HashMap<String, Callable<Object>>();

        programs.put("countOneMillion", this::countOneMillion);
        programs.put("returnConstant", this::returnConstant);

        return programs;
    }

    public Map<String, HummingbirdVM> hvmTestPrograms(){

        var programs = new HashMap<String, HummingbirdVM>();

        programs.put("countOneMillion", countOneMillion);
        programs.put("callOneMillion", callOneMillion);
        programs.put("fibonacci30", fibonacci30);
        programs.put("simpleFiber", simpleFiber);
        programs.put("simpleFunction", simpleFunction);
        programs.put("recursiveCountOneMillion", recursiveCountOneMillion);
        programs.put("goldenRatio", goldenRatio);

        return programs;
    }

    public HummingbirdVM load(String program){

        try{
            var is = getClass().getClassLoader().getResourceAsStream(program);
            Objects.requireNonNull(is, "Test program " + program + " not found");

            var reader = new BufferedReader(new InputStreamReader(is));

            var builder = new StringBuilder();
            var line = reader.readLine();
            while(line != null){
                builder.append(line);
                builder.append('\n');
                line = reader.readLine();
            }

            reader.close();

            var assembler = new Assembler();
            var exe = assembler.assemble(builder);

            return new HummingbirdVM(exe);

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public int countOneMillion(){
        int x = 0;
        while(x < 1000000) {
            x = x + 1;
        }
        return x;
    }

    public int returnConstant(){
        return 1000;
    }


}
