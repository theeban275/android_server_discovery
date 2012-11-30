require 'socket'
require 'json'

puts "Server Started on Port 12345 ..."
PORT = 12345
socket = UDPSocket.new
socket.bind('0.0.0.0', PORT)
loop do
  data, addr = socket.recvfrom(1024)
    object = JSON.parse(data)
    puts "Received broadcast from #{object["ipaddress"]}:#{object["port"]}"
    s = UDPSocket.new
    s.send("Server is @ #{IPSocket.getaddress(Socket.gethostname)}:#{PORT}", 0, object["ipaddress"], object["port"])
    puts "Sending server is @ #{IPSocket.getaddress(Socket.gethostname)}:#{PORT}"
    s.close
end
