import os
import re

dir_path = 'src/java/shared/enums'
for filename in os.listdir(dir_path):
    if not filename.endswith('.java'): continue
    filepath = os.path.join(dir_path, filename)
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    enum_name = filename[:-5]
    
    # We want to extract just the enum constants
    # They are usually between "public enum EnumName {" and ";"
    # Or just find all occurrences of something like:
    # CONSTANT_NAME("Value")
    # Some might have multiple arguments: CONSTANT_NAME(1, "Value")
    # Or just CONSTANT_NAME
    
    # Better approach: parse lines between public enum { ... }
    # until the first semicolon.
    match = re.search(r'public\s+enum\s+' + enum_name + r'\s*\{([^\;\}]+)', content, flags=re.DOTALL)
    if not match:
        print(f"Could not parse {filename}")
        continue
    
    constants_block = match.group(1)
    
    # Now extract the name and the string value
    # E.g. COMPLETED("Hoàn tất") -> name=COMPLETED, val="Hoàn tất"
    # What if it's COMPLETED(1, "Hoàn tất") -> name=COMPLETED, val="Hoàn tất"
    # What if it's COMPLETED -> name=COMPLETED, val="COMPLETED"
    
    # regex for constant: 
    # identifier followed optionally by parens
    constants = []
    for const_match in re.finditer(r'([A-Z0-9_]+)(?:\s*\((.*?)\))?', constants_block):
        name = const_match.group(1)
        args = const_match.group(2)
        val = name
        if args:
            # find the string literal
            str_match = re.search(r'"(.*?)"', args)
            if str_match:
                val = str_match.group(1)
        constants.append((name, val))
    
    # Now generate the new file
    lines = []
    lines.append("package shared.enums;")
    lines.append("")
    lines.append(f"public enum {enum_name} {{")
    
    enum_decls = []
    for name, val in constants:
        enum_decls.append(f'    {name}("{val}")')
    
    lines.append(",\n".join(enum_decls) + ";")
    lines.append("")
    lines.append("    private final String value;")
    lines.append("")
    lines.append(f"    private {enum_name}(String value) {{")
    lines.append("        this.value = value;")
    lines.append("    }")
    lines.append("")
    lines.append("    public String getValue() {")
    lines.append("        return value;")
    lines.append("    }")
    lines.append("")
    lines.append(f"    public static {enum_name} fromValue(String value) {{")
    lines.append("        if (value == null) {")
    lines.append("            return null;")
    lines.append("        }")
    lines.append(f"        for ({enum_name} status : values()) {{")
    lines.append("            if (status.getValue().equals(value)) {")
    lines.append("                return status;")
    lines.append("            }")
    lines.append("        }")
    lines.append("        return null;")
    lines.append("    }")
    lines.append("}")
    lines.append("")
    
    new_content = "\n".join(lines)
    with open(filepath, 'w', encoding='utf-8', newline='\n') as f:
        f.write(new_content)
    print(f"Refactored {filename}")

