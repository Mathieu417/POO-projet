package application.services;

import application.recommended.Recommendation;
import application.database.*;
import application.database.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ApartmentService{

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    {
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }

    @Autowired
    ApartmentRepository apartmentRepository;

    @Autowired
    LoginRepository loginRepository;

    @Autowired
    Recommendation recommendation;

    @Autowired
    ChatRepository chatRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    FileUploadService fileUploadService;

    public Boolean createApartment(Login login, Apartment apartment, MultipartFile image1,MultipartFile image2,MultipartFile image3,MultipartFile image4) throws Exception{
        List<UserRole> listUserRole=  login.getUserRoles();
        int isHost=0;
        for(int i=0;i<listUserRole.size();i++){
            if(listUserRole.get(i).getRole().equals("host")){
                isHost=1;
                break;
            }
        }
        if(isHost==0){
            throw new Exception("User is not a Host");
        }
        apartment.setLogin(login);
        Apartment apartment1=apartmentRepository.save(apartment);
        recommendation.addApartment(apartment1.getApartmentId());
        return true;
    }



    public Boolean authentication(UserDetails userDetails, int apartmentId){

        if(userDetails==null){
            return false;
        }
        Apartment apartment = apartmentRepository.findOne(apartmentId);
        if(apartment==null){
            return false;
        }



    }

    public Boolean authentication(UserDetails userDetails,Apartment apartment){
        if(apartment==null){
            return false;
        }
        return authentication(userDetails,apartment.getApartmentId());
    }

    public Boolean authentication(String username,Apartment apartment){
        return true;
    }

    public Boolean authentication(String username,int apartmentId){
        return true;
    }



    public Boolean editApartment(int apartmentId,Apartment newApartment) throws Exception{
        Apartment apartment=apartmentRepository.findOne(apartmentId);

        apartment.setNbRoom(newApartment.getNbRoom());
        apartment.setMaxPeople(newApartment.getMaxPeople());
        apartment.setMinPeople(newApartment.getMinPeople());
        apartment.setName(newApartment.getName());
        apartment.setSurface(newApartment.getSurface());
        apartment.setclimatisation(newApartment.getclimatisation());
        apartment.setEquipementBebe(newApartment.getEquipementBebe());
        apartment.setRooms(newApartment.getRooms());

        apartmentRepository.save(apartment);
        return true;
    }

    public Boolean newMessageFormHost(String message,int chatId){
        Chat chat=chatRepository.findOne(chatId);
        if(chat==null){
            System.out.println("the chat does not exist");
            return false;
        }
        Message newMessage= new Message();
        newMessage.setChat(chat);
        newMessage.setContent(message);
        Date date = new Date();
        newMessage.setDateTime(date);
        newMessage.setFromCustomer(false);
        messageRepository.save(newMessage);
        return true;
    }

    public Boolean newMessageFromUser(String message, String username, int apartmentId){
        int notCreateNew=0;
        Chat createdChat=null;
        Message newMessage=new Message();
        Apartment apartment=apartmentRepository.findOne(apartmentId);
        List<Chat> chatlist=chatRepository.findAllByApartment(apartment);
        if(username.equals(apartment.getLogin().getUsername())){
            System.out.println("You sent to your self");
            return false;
        }
        for(Chat chat : chatlist){
            if(chat.getLogin().getUsername().equals(username)){
               notCreateNew=1;
               createdChat=chat;
               break;
            }
        }
        if(notCreateNew==1){
            newMessage.setChat(createdChat);
            newMessage.setContent(message);
            Date date = new Date();
            newMessage.setDateTime(date);
            newMessage.setFromCustomer(true);
            messageRepository.save(newMessage);
        }else{
            Chat newChat=new Chat();
            newChat.setApartment(apartment);
            newChat.setLogin(loginRepository.findOne(username));
            createdChat=chatRepository.save(newChat);
            newMessage.setChat(createdChat);
            newMessage.setContent(message);
            newMessage.setFromCustomer(true);
            Date date = new Date();
            newMessage.setDateTime(date);
            messageRepository.save(newMessage);
        }
        return true;
    }

    public List<Map<String,String>> getAvailableDates(int apartmentId){
        Apartment apartment= apartmentRepository.findOne(apartmentId);
        List resultList = new ArrayList<Map<String,String>>();
        for (Availability oneAvailability :
                apartment.getAvailabilities())
        {
            Map returnMap = new HashMap(2);
            returnMap.put("from",simpleDateFormat.format(oneAvailability.getFromAv()));
            returnMap.put("to",simpleDateFormat.format(oneAvailability.getToAv()));
            resultList.add(returnMap);
        }
        return resultList;
    }

    public List<Map<String,String>> getBookedDates(int apartmentId){
        Apartment apartment= apartmentRepository.findOne(apartmentId);
        List resultList = new ArrayList<Map<String,String>>();
        for (BookInfo oneBookInfo :
                apartment.getBookInfos())
        {
            Map returnMap = new HashMap(2);
            returnMap.put("from",simpleDateFormat.format(oneBookInfo.getBookIn()));
            returnMap.put("to",simpleDateFormat.format(oneBookInfo.getBookOut()));
            resultList.add(returnMap);
        }
        return resultList;

    }

