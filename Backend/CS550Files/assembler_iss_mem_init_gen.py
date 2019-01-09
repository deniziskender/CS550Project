#!/usr/bin/python

# CAUTION: run 'dos2unix assembler.py' if you get bad interpreter error 

# notes:
# 1. Supported instructions:
#
#    ISA v1:
#    ============================================================================================
#    Arithmetic & Logic Instructions  | Data Transfer Instructions | Program Control Instructions
#    ============================================================================================
#                    ADD              |             CP             |            BZJ
#                    ADDi             |             CPi            |            BZJi
#                    NAND             |             CPI            |
#                    NANDi            |             CPIi           |
#                    SRL              |                            |
#                    SRLi             |                            |
#                    LT               |                            |
#                    LTi              |                            |
#                    MUL              |                            |
#                    MULi             |                            |
#
#    ISA v2:
#    ============================================================================================
#    Arithmetic & Logic Instructions  | Data Transfer Instructions | Program Control Instructions
#    ============================================================================================
#                    NAND             |             CP             |            BZ
#                    ADD              |             CPi            |            JMP
#                    ADDF             |             CPI            |
#                    ADDi             |             CPIr           |
#                    LT               |                            |
#                    LTF              |                            |
#                    SRL              |                            |
#                    SRLi             |                            |
#                    MUL              |                            |
#                    MULF             |                            |
# 2. -x only generates .coe and .mif files (other memory files will be supported later)
# 3. Default MEM_SIZE is 16384. That is, 16384x32-bit.

import re
import sys, getopt
import linecache
import struct

def print_blank_line(line_count):
    for i in range(line_count):
        print ""

def print_header(char, count):
    print ""
    for i in range(count):
        print char,
    print ""

def print_footer(char, count):
    for i in range(count):
        print char,
    print ""
        
def print_usage(option=1):
    if option == 0:
        print_blank_line(1)
        print "Invalid syntax. Please run the script as follows:"
    print_blank_line(1)
    print "SYNOPSIS"
    print_blank_line(1)
    print 'assembler_iss_mem_init_gen.py -h -i <input_file> -a <isa_version> -s <sim_type> -m <mem_size> -x -v'
    print_blank_line(2)
    print "DESCRIPTION"
    print_blank_line(1)
    print "assembler_iss_mem_init_gen.py\n\tname of this script"
    print_blank_line(1)
    print "-h"
    print "\tdisplay this help"
    print_blank_line(1)
    print "-i, --ifile <input_file>"
    print"\tset assembly code that must be processed"
    print_blank_line(1)
    print "-a, --arch <isa_version>"
    print "\tset architecture\n\t1 - select isa v1\n\t2 - select isa v2"
    print_blank_line(1)
    print "-s <sim_type>\n\tsimulate code\n\t0 - simulate all\n\t1 - simulate step-by-step"
    print_blank_line(1)
    print "-m, --mem_size <mem_size>"
    print "\tset memory size"
    print_blank_line(1)
    print "-x\n\textract memory initialization files"
    print_blank_line(1)
    print "-v\n\tverbose"

# used a small trick posted on stackoverflow to find out if a string should be float or integer (see https://stackoverflow.com/questions/15357422)
def is_float(x):
    try:
        a = float(x)
    except ValueError:
        return False
    else:
        return True

def is_int(x):
    try:
        temp = int(x)
        a = float(x)
        b = int(a)
    except ValueError:
        return False
    else:
        return a == b

class VerySimpleCPUSim:

    def __init__(self, inputfile="", architecture=1, mem_size=1024, sim_type=0, verbose=False):
        self.inputfile = inputfile
        self.architecture = architecture
        if mem_size != 0:
            self.MEM_SIZE = mem_size
        else:
            self.MEM_SIZE = 16384
        self.sim_type = sim_type
        self.verbose = verbose
        
        if self.verbose:
            print "Class instance is created with following parameters:"
            print self.inputfile
            print self.architecture
            print self.MEM_SIZE
            print self.sim_type
            print self.verbose

        # memory arrays
        self.mem_array_sim = [0] * self.MEM_SIZE
        self.mem_array_coe = ["0"] * self.MEM_SIZE
        self.mem_array_mif = ["00000000000000000000000000000000"] * self.MEM_SIZE

        # file names
        self.outputfile_coe = "instr_data_memory.coe"
        self.outputfile_mif = "instr_data_memory.mif"

        self.program_counter = 0
        self.program_counter_prev = 0
    
        # opcode dictionaries
        self.opcodes_v1 = {"ADD" : 0, "ADDi" : 1, "NAND" : 2, "NANDi" : 3, "SRL" : 4, "SRLi" : 5, "LT" : 6, "LTi" : 7, "CP" : 8, "CPi" : 9, "CPI" : 10, "CPIi" : 11, "BZJ" : 12, "BZJi" : 13, "MUL" : 14, "MULi" : 15}

        self.opcodes_v1_r = {0 : "ADD", 1 : "ADDi", 2 : "NAND", 3 : "NANDi", 4 : "SRL", 5 : "SRLi", 6 : "LT",
7 : "LTi", 8 : "CP", 9 : "CPi", 10 : "CPI", 11 : "CPIi", 12 : "BZJ", 13 : "BZJi", 14 : "MUL", 15 : "MULi"}

        self.opcodes_v2 = {"NAND" : 0, "ADD" : 1, "ADDF" : 2, "ADDi" : 3, "LT" : 4, "LTF" : 5, "SRL" : 6, "SRLi" : 7, "MUL" : 8, "MULF" : 9, "CP" : 10, "CPi" : 11, "CPI" : 12, "CPIr" : 13, "BZ" : 14, "JMP" : 15}

        self.opcodes_v2_r = {0 : "NAND", 1 : "ADD", 2 : "ADDF", 3 : "ADDi", 4 : "LT", 5 : "LTF", 6 : "SRL", 7 : "SRLi", 8 : "MUL", 9 : "MULF", 10 : "CP", 11 : "CPi", 12 : "CPI", 13 : "CPIr", 14 : "BZ", 15 : "JMP"}

    def print_error(self, linenum, *operand_index):
        print_header("!", 25)
        print "Error at line", (linenum + 1), ":",
        if len(operand_index) == 2:
            print "operand", operand_index[0], "and operand", operand_index[1], "must be nonnegative integers"
        else:
            print "operand", operand_index[0], "must be nonnegative integer"
        
        line = linecache.getline(self.inputfile, (linenum + 1)).rstrip()
        print line
        print_footer("!", 25)
        sys.exit(1)
    
    def is_instruction_or_data(self, instruction_or_data):
        if self.architecture == 1:
            if instruction_or_data in self.opcodes_v1:
                return 1
            else:
                return 0
        elif self.architecture == 2:
            if instruction_or_data in self.opcodes_v2:
                return 1
            else:
                return 0
        else:
            return 0

    def extract_memory_files(self):
        fo_coe = open(self.outputfile_coe, "w")
        fo_mif = open(self.outputfile_mif, "w")
        fo_coe.write("memory_initialization_radix=16;\n")
        fo_coe.write("memory_initialization_vector=\n")
        for i in range(self.MEM_SIZE):
            fo_coe.write(self.mem_array_coe[i])
            fo_mif.write(self.mem_array_mif[i])
            if i == (self.MEM_SIZE-1):
                fo_coe.write(";")
            else:
                fo_coe.write(",\n")
                fo_mif.write("\n")
        fo_coe.close()
        fo_mif.close()

    def parse_and_create_memory_image(self):
        linenum = 0

        with open(self.inputfile) as fi:
            for line in fi:
            
                new_line_removed = re.sub(r"\n", "", line)
            
                comment_removed = re.sub(r"//.*$", "", new_line_removed)

                if comment_removed == "":
                    continue
                                    
                index = (re.search(r"(\d+)", comment_removed)).group(0)
                                
                instruction_or_data = re.sub(r"(\d+):", "", comment_removed)
                if self.verbose: print instruction_or_data

                regex = r"(\w+|\d*\.\d+|\d+)(\ +)([\-]?\w+)(\ +)([\-]?\w+)"
                
                if self.is_instruction_or_data(re.search(r"(\w+|\d*\.\d+|\d+)", instruction_or_data).group(0)):
                    if self.architecture == 1:
                        opcode = self.opcodes_v1[re.search(regex, instruction_or_data).group(1)]
                    else:
                        opcode = self.opcodes_v2[re.search(regex, instruction_or_data).group(1)]
                    
                    operand_1 = re.search(regex, instruction_or_data).group(3)
                    operand_2 = re.search(regex, instruction_or_data).group(5)
                    
                    if self.architecture == 1 and int(operand_2) < 0:
                        if int(operand_1) < 0:
                            self.print_error(int(linenum), 1, 2)
                        self.print_error(int(linenum), 2)
                    
                    elif int(operand_2) < 0 and \
                       ( opcode == self.opcodes_v2["NAND"] or \
                         opcode == self.opcodes_v2["ADD"]  or \
                         opcode == self.opcodes_v2["ADDF"] or \
                         opcode == self.opcodes_v2["LT"]   or \
                         opcode == self.opcodes_v2["LTF"]  or \
                         opcode == self.opcodes_v2["SRL"]  or \
                         opcode == self.opcodes_v2["SRLi"]  or \
                         opcode == self.opcodes_v2["MUL"]  or \
                         opcode == self.opcodes_v2["MULF"] or \
                         opcode == self.opcodes_v2["CP"] or \
                         opcode == self.opcodes_v2["CPI"] or \
                         opcode == self.opcodes_v2["CPIr"] or \
                         opcode == self.opcodes_v2["BZ"]):
                        if int(operand_1) < 0:
                            self.print_error(int(linenum), 1, 2)
                        self.print_error(int(linenum), 2)

                    if int(operand_1) < 0:
                        self.print_error(int(linenum), 1)
                    
                    # used a small trick posted on stackoverflow to generate hex representation of data in .coe and .mif files (see https://stackoverflow.com/questions/7822956)
                    operand_2 = (int(operand_2) + (1 << 32)) % (1 << 32)
                    operand_2 = 0x3FFF & operand_2
                                       
                    instruction_dec = (opcode << 28) | (int(operand_1) << 14) | int(operand_2)
                    instruction_hex = format(instruction_dec, "08x")
                    instruction_hex = instruction_hex.lstrip("0")
                    self.mem_array_sim[int(index)] = instruction_dec
                    self.mem_array_coe[int(index)] = instruction_hex
                    self.mem_array_mif[int(index)] = format(instruction_dec, "032b")
                else:
                    if is_int(instruction_or_data):
                        
                        if self.architecture == 1 and int(instruction_or_data) < 0:
                            self.print_error(int(linenum), 1)

                        data = int(instruction_or_data)
                        
                        # used a small trick posted on stackoverflow to generate hex representation of data in .coe and .mif files (see https://stackoverflow.com/questions/7822956)
                        data_hex = (int(instruction_or_data) + (1 << 32)) % (1 << 32)
                        data_bin = data_hex
                        data_hex = format(data_hex, "08x")
                        if data_hex == "00000000":
                            data_hex  = "0"
                        else:
                            data_hex = data_hex.lstrip("0")
                            self.mem_array_sim[int(index)] = data
                            self.mem_array_coe[int(index)] = data_hex
                            self.mem_array_mif[int(index)] = format(data_bin, "032b")
                    
                    elif is_float(instruction_or_data):
                        if self.architecture == 1:
                            self.print_error(int(linenum), 1)
                        
                        # used a method posted on stackoverflow to generate hex representation of float data in .coe and .mif files (see https://stackoverflow.com/questions/23624212)
                        data_hex = hex(struct.unpack('<I', struct.pack('<f', float(instruction_or_data)))[0])
                        data_hex = re.sub(r"(0x)", "", data_hex)
                        # used a method posted on stackoverflow to generate binary representation of float data in .coe and .mif files (see https://stackoverflow.com/questions/16444726)
                        data_bin = ''.join(bin(ord(c)).replace('0b', '').rjust(8, '0') for c in struct.pack('!f', float(instruction_or_data)))
                        if data_hex == "00000000":
                            data_hex  = "0"
                        else:
                            data_hex = data_hex.lstrip("0")
                        self.mem_array_sim[int(index)] = float(instruction_or_data)
                        self.mem_array_coe[int(index)] = data_hex
                        self.mem_array_mif[int(index)] = data_bin

                linenum = linenum + 1

    def print_memory_cell(self, opcode, operand_1, operand_2):
        if self.architecture == 1:
            if opcode == self.opcodes_v1["ADD"] or \
               opcode == self.opcodes_v1["NAND"] or \
               opcode == self.opcodes_v1["SRL"] or \
               opcode == self.opcodes_v1["LT"] or \
               opcode == self.opcodes_v1["MUL"] or \
               opcode == self.opcodes_v1["CP"] or \
               opcode == self.opcodes_v1["BZJ"]:
                print "\tmem[", operand_1, "]\t: ", self.mem_array_sim[operand_1]
                print "\tmem[", operand_2, "]\t: ", self.mem_array_sim[operand_2]
            elif opcode == self.opcodes_v1["CPI"]:
                print "\tmem[", operand_1, "]\t: ", self.mem_array_sim[operand_1]
                print "\tmem[", operand_2, "]\t: ", self.mem_array_sim[operand_2]
                print "\tmem[", self.mem_array_sim[operand_2], "]\t: ", self.mem_array_sim[self.mem_array_sim[operand_2]]
            elif opcode == self.opcodes_v1["CPIi"]:
                print "\tmem[", operand_1, "]\t: ", self.mem_array_sim[operand_1]
                print "\tmem[", operand_2, "]\t: ", self.mem_array_sim[operand_2]
                print "\tmem[", self.mem_array_sim[operand_1], "]\t: ", self.mem_array_sim[self.mem_array_sim[operand_1]]
            elif opcode == self.opcodes_v1["ADDi"] or \
                 opcode == self.opcodes_v1["NANDi"] or \
                 opcode == self.opcodes_v1["SRLi"] or \
                 opcode == self.opcodes_v1["LTi"] or \
                 opcode == self.opcodes_v1["MULi"] or \
                 opcode == self.opcodes_v1["CPi"] or \
                 opcode == self.opcodes_v1["BZJi"]:
                print "\tmem[", operand_1, "]          : ", self.mem_array_sim[operand_1]
            
        elif self.architecture == 2:
            if opcode == self.opcodes_v2["NAND"] or \
               opcode == self.opcodes_v2["ADD"]  or \
               opcode == self.opcodes_v2["ADDF"] or \
               opcode == self.opcodes_v2["LT"]   or \
               opcode == self.opcodes_v2["LTF"]  or \
               opcode == self.opcodes_v2["SRL"]  or \
               opcode == self.opcodes_v2["MUL"]  or \
               opcode == self.opcodes_v2["MULF"] or \
               opcode == self.opcodes_v2["CP"] or \
               opcode == self.opcodes_v2["BZ"]:
                print "\tmem[", operand_1, "]\t: ", self.mem_array_sim[operand_1]
                print "\tmem[", operand_2, "]\t: ", self.mem_array_sim[operand_2]
            elif opcode == self.opcodes_v2["CPI"]:
                print "\tmem[", operand_1, "]\t: ", self.mem_array_sim[operand_1]
                print "\tmem[", operand_2, "]\t: ", self.mem_array_sim[operand_2]
                print "\tmem[", self.mem_array_sim[operand_2], "]\t: ", self.mem_array_sim[self.mem_array_sim[operand_2]]
            elif opcode == self.opcodes_v2["CPIr"]:
                print "\tmem[", operand_1, "]\t: ", self.mem_array_sim[operand_1]
                print "\tmem[", operand_2, "]\t: ", self.mem_array_sim[operand_2]
                print "\tmem[", self.mem_array_sim[operand_1], "]\t: ", self.mem_array_sim[self.mem_array_sim[operand_1]]
            elif opcode == self.opcodes_v2["ADDi"] or \
                 opcode == self.opcodes_v2["SRLi"] or \
                 opcode == self.opcodes_v2["CPi"] or \
                 opcode == self.opcodes_v2["JMP"]:
                print "\tmem[", operand_1, "]          : ", self.mem_array_sim[operand_1]
            
    def run_instruction(self, opcode, operand_1, operand_2):

        if self.architecture == 1:
            if (opcode >= 0 and opcode <= 11) or (opcode >= 14 and opcode <= 15):
                print "\tMemory content before executing instruction"
                self.print_memory_cell(opcode, operand_1, operand_2)
                                
                if opcode == self.opcodes_v1["ADD"]:
                    # used a small trick posted on stackoverflow to generate 2's complement representation of data for ISA v1 if needed (see https://stackoverflow.com/questions/1604464)
                    if (self.mem_array_sim[operand_1] & (1 << (32 - 1))) != 0:
                        temp_1 = self.mem_array_sim[operand_1] - (1 << 32)
                    else:
                        temp_1 = self.mem_array_sim[operand_1]

                    if (self.mem_array_sim[operand_2] & (1 << (32 - 1))) != 0:
                        temp_2 = self.mem_array_sim[operand_2] - (1 << 32)
                    else:
                        temp_2 = self.mem_array_sim[operand_2]
                    self.mem_array_sim[operand_1] = (temp_1 + temp_2) & 0xFFFFFFFF
                elif opcode == self.opcodes_v1["ADDi"]:
                    # used a small trick posted on stackoverflow to generate 2's complement representation of data for ISA v1 if needed (see https://stackoverflow.com/questions/1604464)
                    if (self.mem_array_sim[operand_1] & (1 << (32 - 1))) != 0:
                        temp_1 = self.mem_array_sim[operand_1] - (1 << 32)
                    else:
                        temp_1 = self.mem_array_sim[operand_1]
                    self.mem_array_sim[operand_1] = (temp_1 + operand_2) & 0xFFFFFFFF
                elif opcode == self.opcodes_v1["NAND"]:
                    temp_1 = self.mem_array_sim[operand_1]
                    temp_2 = self.mem_array_sim[operand_2]
                    self.mem_array_sim[operand_1] = (~(temp_1 & temp_2)) & 0xFFFFFFFF
                elif opcode == self.opcodes_v1["NANDi"]:
                    temp_1 = self.mem_array_sim[operand_1]
                    self.mem_array_sim[operand_1] = (~(temp_1 & operand_2) & 0xFFFFFFFF) 
                elif opcode == self.opcodes_v1["SRL"]: # CAUTION: it does not handle negative shift values
                    if self.mem_array_sim[operand_2] < 32:
                        # used a method posted on stackoverflow to get logical shift (see https://stackoverflow.com/questions/5832982)
                        self.mem_array_sim[operand_1] =  (self.mem_array_sim[operand_1] % 0x100000000) >> self.mem_array_sim[operand_2]
                    else:
                        self.mem_array_sim[operand_1] = self.mem_array_sim[operand_1] << (self.mem_array_sim[operand_2] - 32)
                elif opcode == self.opcodes_v1["SRLi"]:
                    if operand_2 < 32:
                        self.mem_array_sim[operand_1] =  (self.mem_array_sim[operand_1] % 0x100000000) >> operand_2
                    else:
                        self.mem_array_sim[operand_1] = temp_1 << (operand_2 - 32)
                elif opcode == self.opcodes_v1["LT"]:
                    if self.mem_array_sim[operand_1] < self.mem_array_sim[operand_2]:
                        self.mem_array_sim[operand_1] = 1
                    else:
                        self.mem_array_sim[operand_1] = 0
                elif opcode == self.opcodes_v1["LTi"]:
                    if self.mem_array_sim[operand_1] < operand_2:
                        self.mem_array_sim[operand_1] = 1
                    else:
                        self.mem_array_sim[operand_1] = 0
                elif opcode == self.opcodes_v1["MUL"]:
                    # used a small trick posted on stackoverflow to generate 2's complement representation of data for ISA v1 if needed (see https://stackoverflow.com/questions/1604464)
                    if (self.mem_array_sim[operand_1] & (1 << (32 - 1))) != 0:
                        temp_1 = self.mem_array_sim[operand_1] - (1 << 32)
                    else:
                        temp_1 = self.mem_array_sim[operand_1]

                    if (self.mem_array_sim[operand_2] & (1 << (32 - 1))) != 0:
                        temp_2 = self.mem_array_sim[operand_2] - (1 << 32)
                    else:
                        temp_2 = self.mem_array_sim[operand_2]
                    self.mem_array_sim[operand_1] = (temp_1 * temp_2) & 0xFFFFFFFF
                elif opcode == self.opcodes_v1["MULi"]:
                    self.mem_array_sim[operand_1] = self.mem_array_sim[operand_1] * operand_2
                elif opcode == self.opcodes_v1["CP"]:
                    self.mem_array_sim[operand_1] = self.mem_array_sim[operand_2]
                elif opcode == self.opcodes_v1["CPi"]:
                    self.mem_array_sim[operand_1] = operand_2
                elif opcode == self.opcodes_v1["CPI"]:
                    temp = self.mem_array_sim[operand_2]
                    self.mem_array_sim[operand_1] = self.mem_array_sim[temp]
                elif opcode == self.opcodes_v1["CPIi"]:
                    temp_1 = self.mem_array_sim[operand_1]
                    temp_2 = self.mem_array_sim[operand_2]
                    self.mem_array_sim[temp_1] = temp_2
                
                self.program_counter = self.program_counter + 1
            
                print "\tMemory content after executing instruction"
                self.print_memory_cell(opcode, operand_1, operand_2)
            elif opcode == self.opcodes_v1["BZJ"]:
                self.print_memory_cell(opcode, operand_1, operand_2)
                if self.mem_array_sim[operand_2] == 0:
                    self.program_counter = self.mem_array_sim[operand_1]
                else:
                    self.program_counter = self.program_counter + 1
            elif opcode == self.opcodes_v1["BZJi"]:
                self.print_memory_cell(opcode, operand_1, operand_2)
                self.program_counter = self.mem_array_sim[operand_1] + operand_2
            else:
                print "UNDEFINED INSTRUCTION"
                sys.exit(1)
        elif self.architecture == 2:
            if opcode >= 0 and opcode <= 13:
                print "\tMemory content before executing instruction"
                self.print_memory_cell(opcode, operand_1, operand_2)

                if opcode == self.opcodes_v2["NAND"]:
                    self.mem_array_sim[operand_1] = ~(self.mem_array_sim[operand_1] & self.mem_array_sim[operand_2])
                elif opcode == self.opcodes_v2["ADD"]:
                    self.mem_array_sim[operand_1] = self.mem_array_sim[operand_1] + self.mem_array_sim[operand_2]
                elif opcode == self.opcodes_v2["ADDF"]:
                    self.mem_array_sim[operand_1] = float(self.mem_array_sim[operand_1]) + float(self.mem_array_sim[operand_2])
                elif opcode == self.opcodes_v2["ADDi"]:
                    self.mem_array_sim[operand_1] = self.mem_array_sim[operand_1] + operand_2
                elif opcode == self.opcodes_v2["LT"]:
                    if self.mem_array_sim[operand_1] < self.mem_array_sim[operand_2]:
                        self.mem_array_sim[operand_1] = 1
                    else:
                        self.mem_array_sim[operand_1] = 0
                elif opcode == self.opcodes_v2["LTF"]:
                    if float(self.mem_array_sim[operand_1]) < float(self.mem_array_sim[operand_2]):
                        self.mem_array_sim[operand_1] = 1
                    else:
                        self.mem_array_sim[operand_1] = 0
                elif opcode == self.opcodes_v2["SRL"]: # CAUTION: it does not handle negative shift values
                    if self.mem_array_sim[operand_2] < 32:
                        self.mem_array_sim[operand_1] = self.mem_array_sim[operand_1] >> self.mem_array_sim[operand_2]
                    else:
                        self.mem_array_sim[operand_1] = self.mem_array_sim[operand_1] << (self.mem_array_sim[operand_2] - 32)
                elif opcode == self.opcodes_v2["SRLi"]:
                    if operand_2 < 32:
                        self.mem_array_sim[operand_1] = self.mem_array_sim[operand_1] >> operand_2
                    else:
                        self.mem_array_sim[operand_1] = self.mem_array_sim[operand_1] << (operand_2 - 32)
                elif opcode == self.opcodes_v2["MUL"]:
                    self.mem_array_sim[operand_1] = self.mem_array_sim[operand_1] * self.mem_array_sim[operand_2]
                elif opcode == self.opcodes_v2["MULF"]:
                    self.mem_array_sim[operand_1] = float(self.mem_array_sim[operand_1]) * float(self.mem_array_sim[operand_2])
                elif opcode == self.opcodes_v2["CP"]:
                    self.mem_array_sim[operand_1] = self.mem_array_sim[operand_2]
                elif opcode == self.opcodes_v2["CPi"]:
                    self.mem_array_sim[operand_1] = operand_2
                elif opcode == self.opcodes_v2["CPI"]:
                    temp = self.mem_array_sim[operand_2]
                    self.mem_array_sim[operand_1] = self.mem_array_sim[temp]
                elif opcode == self.opcodes_v2["CPIr"]:
                    temp_1 = self.mem_array_sim[operand_1]
                    temp_2 = self.mem_array_sim[operand_2]
                    self.mem_array_sim[temp_1] = temp_2
                
                self.program_counter = self.program_counter + 1
            
                print "\tMemory content after executing instruction"
                self.print_memory_cell(opcode, operand_1, operand_2)
            elif opcode == self.opcodes_v2["BZ"]:
                self.print_memory_cell(opcode, operand_1, operand_2)
                if self.mem_array_sim[operand_2] == 0:
                    self.program_counter = self.mem_array_sim[operand_1]
                else:
                    self.program_counter = self.program_counter + 1
            elif opcode == self.opcodes_v2["JMP"]:
                self.print_memory_cell(opcode, operand_1, operand_2)
                self.program_counter = self.mem_array_sim[operand_1] + operand_2
            else:
                print "UNDEFINED INSTRUCTION"
                sys.exit(1)            

    def run_simulation(self):

        print_blank_line(1)
        print "Starting simulation"
        print_blank_line(1)
        
        if self.sim_type == 0:
            print "run all mode selected"
        else:
            print "step-by-step mode selected"

        print_blank_line(1)      

        # CAUTION: it is assumed that instructions are always placed starting from very beginning of memory, i.e., address zero
        # parsing bits [31-28] to catch an instruction

        sim_finished = False
        
        while(sim_finished == False):

            if self.sim_type == 1:
                raw_input("\nPress any key to continue ")

            instruction_or_data = self.mem_array_sim[self.program_counter]
            if (instruction_or_data & 0xFFFFFFFF) > 0: # CAUTION: it is assumed that instructions such as 'ADD 0 0' or 'NAND 0 0' are not generated by compiler

                opcode = instruction_or_data >> 28
                operand_1 = (instruction_or_data >> 14) & 0x3FFF
                operand_2 = instruction_or_data & 0x3FFF

                print_header("*", 25)
                if self.architecture == 1:
                    print "\tcurrent_instruction: ", self.opcodes_v1_r[opcode], operand_1, operand_2
                elif self.architecture == 2:
                    print "\tcurrent_instruction: ", self.opcodes_v2_r[opcode], operand_1, operand_2
                print "\tprogram counter    : ", self.program_counter
                self.program_counter_prev = self.program_counter
                self.run_instruction(opcode, operand_1, operand_2)
                print_footer("*", 25)

                if self.program_counter_prev == self.program_counter:
                    print "Finishing simulation. Instruction called itself."
                    sim_finished = True
            else:
                print "Finishing simulation"
                sim_finished = True
                
def main(argv):
   
    try:
        opts, args = getopt.getopt(argv,"hi:s:a:m:vx",["ifile=", "extract_memory_files", "sim=", "arch=", "memory_size="])
    except getopt.GetoptError:
        print_usage()
        sys.exit(1)

    inputfile = ""
    extract = False
    verbose = False
    simulate = False
    sim_type = 0
    architecture = 0
    mem_size = 0
    
    if not opts:
        print_usage()
        sys.exit(1)
        
    for opt, arg in opts:
        if opt == '-h':
            print_usage(1)
            sys.exit(1)
        elif opt in ("-i", "--ifile"):
            inputfile = arg
        elif opt in ("-a", "--arch"):
            architecture = int(arg)
        elif opt in ("-s", "--sim"):
            simulate = True
            sim_type = int(arg)
        elif opt in ("-m", "--mem_size"):
            mem_size = int(arg)
        elif opt in ("-x", "--extract_memory_files"):
            extract = True
        elif opt in ("-v"):
            verbose = True
            print "Verbosing output"

    if inputfile == "" or architecture == 0 or mem_size < 0:
        print_usage(0)
        sys.exit(1)

    MyVerySimpleCPUSim = VerySimpleCPUSim(inputfile=inputfile, architecture=architecture, mem_size=mem_size, sim_type=sim_type, verbose=verbose)
    
    MyVerySimpleCPUSim.parse_and_create_memory_image()

    if simulate == True:
        MyVerySimpleCPUSim.run_simulation()

    if extract == True:
        print "Extracting memory initialization files"
        MyVerySimpleCPUSim.extract_memory_files()

if __name__ == "__main__":
    main(sys.argv[1:])
