/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

// interface must extend Remote, and methods must throw RemoteException
public interface ChatService extends Remote {
    void send(String message) throws RemoteException;
}