/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package l3s.de.ranking_or;

import java.io.Serializable;

/**
 *
 * @author Pavlos Fafalios
 */
public class Triple implements Serializable {

    private Object subject;
    private Object predicate;
    private Object object;

    public Triple(Object subject, Object predicate, Object object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public Object getSubject() {
        return subject;
    }

    public void setSubject(Object subject) {
        this.subject = subject;
    }

    public Object getPredicate() {
        return predicate;
    }

    public void setPredicate(Object predicate) {
        this.predicate = predicate;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    @Override
    public String toString() {
        
        String s = subject.toString();
        String p = predicate.toString();
        String o = object.toString();

        return s + "\t" + p + "\t" + o;
    }
}
