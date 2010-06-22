/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\data\\dan\\BaardTERM\\src\\com\\happygoatstudios\\bt\\service\\IBaardTERMServiceCallback.aidl
 */
package com.happygoatstudios.bt.service;
public interface IBaardTERMServiceCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.happygoatstudios.bt.service.IBaardTERMServiceCallback
{
private static final java.lang.String DESCRIPTOR = "com.happygoatstudios.bt.service.IBaardTERMServiceCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.happygoatstudios.bt.service.IBaardTERMServiceCallback interface,
 * generating a proxy if needed.
 */
public static com.happygoatstudios.bt.service.IBaardTERMServiceCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.happygoatstudios.bt.service.IBaardTERMServiceCallback))) {
return ((com.happygoatstudios.bt.service.IBaardTERMServiceCallback)iin);
}
return new com.happygoatstudios.bt.service.IBaardTERMServiceCallback.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_dataIncoming:
{
data.enforceInterface(DESCRIPTOR);
byte[] _arg0;
_arg0 = data.createByteArray();
this.dataIncoming(_arg0);
reply.writeByteArray(_arg0);
return true;
}
case TRANSACTION_processedDataIncoming:
{
data.enforceInterface(DESCRIPTOR);
java.lang.CharSequence _arg0;
if ((0!=data.readInt())) {
_arg0 = android.text.TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.processedDataIncoming(_arg0);
return true;
}
case TRANSACTION_htmlDataIncoming:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.htmlDataIncoming(_arg0);
return true;
}
case TRANSACTION_rawDataIncoming:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.rawDataIncoming(_arg0);
return true;
}
case TRANSACTION_rawBufferIncoming:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.rawBufferIncoming(_arg0);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.happygoatstudios.bt.service.IBaardTERMServiceCallback
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public void dataIncoming(byte[] seq) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByteArray(seq);
mRemote.transact(Stub.TRANSACTION_dataIncoming, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
public void processedDataIncoming(java.lang.CharSequence seq) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((seq!=null)) {
_data.writeInt(1);
android.text.TextUtils.writeToParcel(seq, _data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_processedDataIncoming, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
public void htmlDataIncoming(java.lang.String html) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(html);
mRemote.transact(Stub.TRANSACTION_htmlDataIncoming, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
public void rawDataIncoming(java.lang.String raw) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(raw);
mRemote.transact(Stub.TRANSACTION_rawDataIncoming, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
public void rawBufferIncoming(java.lang.String rawbuf) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(rawbuf);
mRemote.transact(Stub.TRANSACTION_rawBufferIncoming, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_dataIncoming = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_processedDataIncoming = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_htmlDataIncoming = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_rawDataIncoming = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_rawBufferIncoming = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
}
public void dataIncoming(byte[] seq) throws android.os.RemoteException;
public void processedDataIncoming(java.lang.CharSequence seq) throws android.os.RemoteException;
public void htmlDataIncoming(java.lang.String html) throws android.os.RemoteException;
public void rawDataIncoming(java.lang.String raw) throws android.os.RemoteException;
public void rawBufferIncoming(java.lang.String rawbuf) throws android.os.RemoteException;
}
