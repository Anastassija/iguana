module excepts::Test2

syntax S = E;

syntax E = star: E!plus "*" E!star!hat
         | hat: E!minus!plus "^" E!hat
         | minus: "-" E
         | plus: "+" E
         | "a";
         
public str input1 = "-a^a";   // -(a^a)
public str input2 = "+a^a";   // +(a^a)
public str input3 = "a^+a*a"; // (a^(+a))*a || a^(+(a*a))
public str input4 = "a^a^a";  // (a^a)^a
public str input5 = "a*a^a";  // (a*a)^a

public loc l1 = |file:///Users/anastasiaizmaylova/git/diguana/test/org/jgll/parser/datadependent/excepts/Test2_1.java|;
public loc l2 = |file:///Users/anastasiaizmaylova/git/diguana/test/org/jgll/parser/datadependent/excepts/Test2_2.java|;
public loc l3 = |file:///Users/anastasiaizmaylova/git/diguana/test/org/jgll/parser/datadependent/excepts/Test2_3.java|;
public loc l4 = |file:///Users/anastasiaizmaylova/git/diguana/test/org/jgll/parser/datadependent/excepts/Test2_4.java|;
public loc l5 = |file:///Users/anastasiaizmaylova/git/diguana/test/org/jgll/parser/datadependent/excepts/Test2_5.java|;

public void main() {
	generate(#S,input1,l1);
	generate(#S,input2,l2);
	generate(#S,input3,l3);
	generate(#S,input4,l4);
	generate(#S,input5,l5);
}