from bluetooth import *
hostMACAddress = 'B8:27:EB:DC:AC:3E'
port = 0
backlog = 1
size = 1024
#uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"
uuid = "01195ea7-dc96-4750-9c34-4d28e110f201"
s = BluetoothSocket(RFCOMM)
s.bind((hostMACAddress, PORT_ANY))
s.listen(backlog)
port = s.getsockname()[1]
print("Port = ", port)
advertise_service( s, "Nasinge",
                   service_id = uuid,
                   service_classes = [ uuid, SERIAL_PORT_CLASS ],
                   profiles = [ SERIAL_PORT_PROFILE ]
                 # protocols = [ OBEX_UUID]
                   )
try:
    print("Connecting")
    client, clientInfo = s.accept()
    while 1:
        print("Connected")
        data = client.recv(size)
        
        if data:
           print(data)
           client.send(data)
except:
    print("Closing socket");
    client.close()
    s.close()
    