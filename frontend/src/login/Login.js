import React, { useState } from 'react'
import background from '../image/logo192.png'
import Home from '../home/Home' ;
import { BrowserRouter as Router , Routes, Route , Redirect} from 'react-router-dom';
import axios from 'axios';




function Login() {

    const [login, setLogin] = useState({
        username: "",
        password: ""
    });


    const checkLogin = () => {
        console.log(login);
        let check = false ;
        axios.get(`http://localhost:8080/user/api/user/login?email=${login.username}&password=${login.password}`).then(res => {
            console.log(res.data.data);
            console.log("Success");
            const token = res.data.data.userID ;
            console.log(token);
            localStorage.setItem('token', token);
            window.location.href = '/home' ;
        })

        return check ;
    }
    
    const handle = (e) => {

        console.log(e.target.name);
        console.log(e.target.value); 

        setLogin((loginData) => {
            return{
                ...loginData,
                [e.target.name]:e.target.value
            }
        })
    }
    
    const submit = (e) => {
        e.preventDefault();
        checkLogin();
    }



  return (
    <div style = {{backgroundImage: `url(${background})`}}>
        Login Page
        <form onSubmit={submit}>
            <h1>Username</h1>
            <input onChange={handle} name = "username" type = "text" />
            <h1>Password</h1>
            <input onChange={handle} name = "password"type = "text" /><br></br><br></br>
            <button type = "submit">Login</button>
        </form>
    </div>

  )
}

export default Login
