/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\data\\dan\\BaardTERM\\src\\com\\happygoatstudios\\bt\\service\\IBaardTERMService.aidl
 */
package com.happygoatstudios.bt.service;
public interface IBaardTERMService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.happygoatstudios.bt.service.IBaardTERMService
{
private static final java.lang.String DESCRIPTOR = "com.happygoatstudios.bt.service.IBaardTERMService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.happygoatstudios.bt.service.IBaardTERMService interface,
 * generating a proxy if needed.
 */
public static com.happygoatstudios.bt.service.IBaardTERMService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.happygoatstudios.bt.service.IBaardTERMService))) {
return ((com.happygoatstudios.bt.service.IBaardTERMService)iin);
}
return new com.happygoatstudios.bt.service.IBaardTERMService.Stub.Proxy(obj);
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
case TRANSACTION_registerCallback:
{
data.enforceInterface(DESCRIPTOR);
com.happygoatstudios.bt.service.IBaardTERMServiceCallback _arg0;
_arg0 = com.happygoatstudios.bt.service.IBaardTERMServiceCallback.Stub.asInterface(data.readStrongBinder());
this.registerCallback(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_unregisterCallback:
{
data.enforceInterface(DESCRIPTOR);
com.happygoatstudios.bt.service.IBaardTERMServiceCallback _arg0;
_arg0 = com.happygoatstudios.bt.service.IBaardTERMServiceCallback.Stub.asInterface(data.readStrongBinder());
this.unregisterCallback(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getPid:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getPid();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_initXfer:
{
data.enforceInterface(DESCRIPTOR);
this.initXfer();
reply.writeNoException();
return true;
}
case TRANSACTION_endXfer:
{
data.enforceInterface(DESCRIPTOR);
this.endXfer();
reply.writeNoException();
return true;
}
case TRANSACTION_sendData:
{
data.enforceInterface(DESCRIPTOR);
byte[] _arg0;
_arg0 = data.createByteArray();
this.sendData(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setNotificationText:
{
data.enforceInterface(DESCRIPTOR);
java.lang.CharSequence _arg0;
if ((0!=data.readInt())) {
_arg0 = android.text.TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.setNotificationText(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setConnectionData:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
this.setConnectionData(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_beginCompression:
{
data.enforceInterface(DESCRIPTOR);
this.beginCompression();
reply.writeNoException();
return true;
}
case TRANSACTION_stopCompression:
{
data.enforceInterface(DESCRIPTOR);
this.stopCompression();
reply.writeNoException();
return true;
}
case TRANSACTION_requestBuffer:
{
data.enforceInterface(DESCRIPTOR);
this.requestBuffer();
reply.writeNoException();
return true;
}
case TRANSACTION_saveBuffer:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.saveBuffer(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.happygoatstudios.bt.service.IBaardTERMService
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
public void registerCallback(com.happygoatstudios.bt.service.IBaardTERMServiceCallback c) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((c!=null))?(c.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void unregisterCallback(com.happygoatstudios.bt.service.IBaardTERMServiceCallback c) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((c!=null))?(c.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unregisterCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public int getPid() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getPid, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void initXfer() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_initXfer, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void endXfer() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_endXfer, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void sendData(byte[] seq) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByteArray(seq);
mRemote.transact(Stub.TRANSACTION_sendData, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void setNotificationText(java.lang.CharSequence seq) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((seq!=null)) {
_data.writeInt(1);
android.text.TextUtils.writeToParcel(seq, _data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_setNotificationText, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void setConnectionData(java.lang.String host, int port) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(host);
_data.writeInt(port);
mRemote.transact(Stub.TRANSACTION_setConnectionData, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void beginCompression() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_beginCompression, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void stopCompression() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_stopCompression, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void requestBuffer() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_requestBuffer, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void saveBuffer(java.lang.String buffer) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(buffer);
mRemote.transact(Stub.TRANSACTION_saveBuffer, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_registerCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_unregisterCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getPid = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_initXfer = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_endXfer = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_sendData = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_setNotificationText = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_setConnectionData = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_beginCompression = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_stopCompression = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_requestBuffer = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_saveBuffer = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
}
public void registerCallback(com.happygoatstudios.bt.service.IBaardTERMServiceCallback c) throws android.os.RemoteException;
public void unregisterCallback(com.happygoatstudios.bt.service.IBaardTERMServiceCallback c) throws android.os.RemoteException;
public int getPid() throws android.os.RemoteException;
public void initXfer() throws android.os.RemoteException;
public void endXfer() throws android.os.RemoteException;
public void sendData(byte[] seq) throws android.os.RemoteException;
public void setNotificationText(java.lang.CharSequence seq) throws android.os.RemoteException;
public void setConnectionData(java.lang.String host, int port) throws android.os.RemoteException;
public void beginCompression() throws android.os.RemoteException;
public void stopCompression() throws android.os.RemoteException;
public void requestBuffer() throws android.os.RemoteException;
public void saveBuffer(java.lang.String buffer) throws android.os.RemoteException;
}
