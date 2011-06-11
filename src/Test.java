public class Test {
	int val;
	Test(int v){
		this.val=v;
	}
	int getVal(){
		return this.val;
	}
	public static void main(String[] args) {
		Test t=new Test(10);
		int v=t.getVal();
		System.out.println(v);
	}

}
