package myworld.hummingbird.assembler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Labels {

    protected final Map<String, Integer> resolvedLabels;
    protected final Map<String, List<PendingOpcode>> unresolvedLabelUses;

    public Labels(){
        resolvedLabels = new HashMap<>();
        unresolvedLabelUses = new HashMap<>();
    }

    public void markResolved(String label, int opcodeIndex) throws AssemblyException {
        if(resolvedLabels.containsKey(label)){
            throw new AssemblyException("Label is already defined: " + label);
        }
        resolvedLabels.put(label, opcodeIndex);
    }

    public void markUnresolvedUse(String label, PendingOpcode opcode){
        unresolvedLabelUses.computeIfAbsent(label, k -> new ArrayList<>())
                .add(opcode);
    }

    public boolean isResolved(Label label){
        return isResolved(label.name());
    }

    public boolean isResolved(String label){
        return resolvedLabels.containsKey(label);
    }

    public int getResolvedIndex(String label) throws AssemblyException {
        if(isResolved(label)){
            return resolvedLabels.get(label);
        }
        throw new AssemblyException("Unresolved label: " + label);
    }

    public Map<String, List<PendingOpcode>> getUnresolvedLabelUses(){
        return unresolvedLabelUses;
    }

}
