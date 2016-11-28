/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.topnl;

/**
 *
 * @author meine
 */
public enum TopNLType {
    TOP50NL ("top50nl"), 
    TOP100NL("top100nl"), 
    TOP250NL("top250nl");
    
    private final String type;
    
    TopNLType(String type){
        this.type =  type;
    }

    public String getType() {
        return type;
    }
    
}
