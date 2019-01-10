0: BZJi 3 17 // Goto main -- Jump to main
// $REGISTERS_SECTION:
1: 45 // Address of $main_base -- SP
2: 45 // Address of $main_base -- BP
3: 0 // Zero
4: 4294967295 // Negative one
5: 0 // VSCPU Special 1
6: 0 // VSCPU Special 2
7: 0 // VSCPU Special 3
8: 0 // VSCPU Special 4
9: 0 // VSCPU Special 5
10: 0 // VSCPU Special 6
11: 0 // GP Reg 1
12: 0 // GP Reg 2
13: 0 // GP Reg 3
14: 0 // GP Reg 4
15: 0 // GP Reg 5
16: 0 // GP Reg 6
// $TEXT_SECTION:
// main:
17: CPIi 1 41 // Push int literal 30
18: ADDi 1 1
19: CPIi 1 42 // Push int literal 62
20: ADDi 1 1
21: ADD 1 4
22: CPI 12 1 // Pop REG_2
23: ADD 1 4
24: CPI 11 1 // Pop REG_1
25: ADD 11 12
26: CPIi 1 11 // Push REG_1
27: ADDi 1 1
28: ADD 1 4
29: CPI 11 1 // Pop REG_1
30: CP 1 2 // Throw away locals and temps
31: ADD 1 4
32: CPI 2 1 // Pop REG_BP
33: ADD 1 4
34: CPI 12 1 // Pop REG_2
35: CPIi 1 11 // Push REG_1
36: ADDi 1 1
37: BZJi 12 0 // End of the function. Go back to the caller
// $main_return:
38: ADD 1 4
39: CPI 11 1 // Pop stack into REG_1
// $HALT:
40: BZJi 3 40 // Goto $HALT -- Instruction jumps to itself to terminate the process.
// $CONSTANT_DATA_SECTION:
// @30:
41: 30 // TYPE(int)
// @62:
42: 62 // TYPE(int)
// $GLOBAL_DATA_SECTION:
// $STACK_SECTION:
43: 38 // Address of $main_return -- Return address (RA) of main
44: 0 // A dummy oldBP value for main's stack frame
// $main_base:
