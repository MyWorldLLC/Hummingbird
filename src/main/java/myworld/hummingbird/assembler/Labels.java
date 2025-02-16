package myworld.hummingbird.assembler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Labels {

    protected final Map<String, Integer> resolvedLabels;
    protected final Map<Label, List<UnresolvedLabelHandler>> unresolvedLabelUses;

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

    public void markUnresolvedUse(Label label, UnresolvedLabelHandler user){
        unresolvedLabelUses.computeIfAbsent(label, k -> new ArrayList<>())
                .add(user);
    }

    public boolean isResolved(Label label){
        return isResolved(label.name());
    }

    public boolean isResolved(String label){
        return resolvedLabels.containsKey(label);
    }

    public int getResolvedIndex(Label label) throws AssemblyException {
        if(isResolved(label.name())){
            var target = resolvedLabels.get(label.name());
            return label.isHotJump() ? -target : target;
        }
        throw new AssemblyException("Unresolved label: " + label);
    }

    public Map<Label, List<UnresolvedLabelHandler>> getUnresolvedLabelUses(){
        return unresolvedLabelUses;
    }

}
