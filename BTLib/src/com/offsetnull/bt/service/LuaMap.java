package com.offsetnull.bt.service;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaObject;
import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

class LuaMap implements  Map {
    private LuaState L;
    private LuaObject table;

    /**
     * Initializes the Luastate used and the table
     */
    public LuaMap() {
        L = LuaStateFactory.newLuaState();
        L.openLibs();
        L.newTable();
        table = L.getLuaObject(-1);
        L.pop(1);
    }

    protected void finalize() throws Throwable {
        super .finalize();
        L.close();
    }

    /**
     * @see java.util.Map#size()
     */
    public int size() {
        table.push();
        L.pushNil();

        int n;
        for (n = 0; L.next(-2) != 0; n++)
            L.pop(1);

        L.pop(2);

        return n;
    }

    /**
     * @see java.util.Map#clear()
     */
    public void clear() {
        L.newTable();
        table = L.getLuaObject(-1);
        L.pop(1);
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        try {
            L.pushObjectValue(key);
            LuaObject obj = L.getLuaObject(-1);
            L.pop(1);

            LuaObject temp = L.getLuaObject(table, obj);

            return !temp.isNil();
        } catch (LuaException e) {
            return false;
        }
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        try {
            L.pushObjectValue(value);
            table.push();
            L.pushNil();

            while (L.next(-2) != 0)/* `key' is at index -2 and `value' at index -1 */
            {
                if (L.equal(-4, -1) != 0) {
                    L.pop(4);
                    return true;
                }
                L.pop(1);
            }

            L.pop(3);
            return false;
        } catch (LuaException e) {
            return false;
        }
    }

    /**
     * not implemented
     * @see java.util.Map#values()
     */
    public Collection values() {
        throw new RuntimeException("not implemented");
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map t) {
        Iterator i = t.keySet().iterator();
        while (i.hasNext()) {
            Object key = i.next();
            put(key, t.get(key));
        }
    }

    /**
     * @see java.util.Map#entrySet()
     */
    public Set entrySet() {
        throw new RuntimeException("not implemented");
    }

    /**
     * @see java.util.Map#keySet()
     */
    public Set keySet() {
        throw new RuntimeException("not implemented");
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {
        try {
            table.push();
            L.pushObjectValue(key);

            L.getTable(-2);

            Object ret = L.toJavaObject(-1);

            L.pop(2);

            return ret;
        } catch (LuaException e) {
            return null;
        }
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key) {
        try {
            Object ret = get(key);

            table.push();
            L.pushObjectValue(key);
            L.pushNil();

            L.setTable(-3);

            L.pop(2);

            return ret;
        } catch (LuaException e) {
            return null;
        }
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(Object key, Object value) {
        try {
            Object ret = get(key);

            table.push();
            L.pushObjectValue(key);
            L.pushObjectValue(value);

            L.setTable(-3);

            L.pop(1);

            return ret;
        } catch (LuaException e) {
            return null;
        }
    }

}
