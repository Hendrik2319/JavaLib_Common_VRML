package net.schwarzbaer.java.lib.system;

public class ValueContainer<Value>
{
	public Value value;
	public ValueContainer(Value value) { this.value = value; }
	public Value getValue()            { return value; }
	public void  setValue(Value value) { this.value = value; }
}
