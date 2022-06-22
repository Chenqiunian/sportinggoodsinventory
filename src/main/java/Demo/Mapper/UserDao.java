package Demo.Mapper;

import Demo.Entity.LoginRecord;
import Demo.Entity.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDao{

	@Autowired(required = false)
	UserMapper userMapper;

	public List<User> selectall(){
		return userMapper.selectall();
	}
	public User selectByEmailAddress(String email){
		User user =  userMapper.selectByEmailAddress(email);
		return user;
	}
	public Boolean checkExist(String username){
		User user = userMapper.checkExist(username);
		if(user==null)
			return false;
		else
			return true;
	}
	public User userLogin(String username,String password){
		User user = userMapper.userLogin(username,password);
		return user;
	}
	public int updateByPrimaryKeySelective(User user){
		int result = userMapper.updateByPrimaryKeySelective(user);
		return result;
	}
	public User selectByPrimaryKey(int id){
		User user = userMapper.selectByPrimaryKey(id);
		return user;
	}
	public Boolean selectByEmail(String email){
		User user = userMapper.selectByEmail(email);
		if(user != null)
			return true;
		else
			return false;
	}
	public int getNewId(){
		int id = 0;
		for(int i = 1;i<1000;i++){
			User user = userMapper.selectByPrimaryKey(i);
			if(user == null){
				id = i;
				System.out.println("获取到新用户ID:"+id);
				break;
			}
		}
		return id;
	}

	public int insertSelective(User user){
		int i = userMapper.insertSelective(user);
		return i;
	}

	public int deleteByPrimaryKey(int[] ids){
		int result = 0;
		for(int i = 0;i<ids.length;i++){
			if(userMapper.deleteByPrimaryKey(ids[i])>0)
				result++;
		}
		return result;
	}

	public int updatePasswordById(int[] ids,String password){
		int result = 0;
		for(int i = 0;i<ids.length;i++){
			if(userMapper.updatePasswordById(ids[i],password)>0){
				result++;
			}
		}

		return result;
	}



}
