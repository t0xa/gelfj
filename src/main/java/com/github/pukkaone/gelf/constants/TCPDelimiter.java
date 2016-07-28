package com.github.pukkaone.gelf.constants;

public enum TCPDelimiter
{
  NULL("\0"),NEWLINE("\n");
	
  private final String delimiter;
  
  TCPDelimiter(String delimiter)
  {
	  this.delimiter = delimiter;
  }
  
  @Override
  public String toString() 
  {
      return delimiter;
  }
}
