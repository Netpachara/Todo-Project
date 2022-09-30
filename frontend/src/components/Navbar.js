//create from rfce shortcut
import React, {useState} from 'react'
import { Link, NavLink } from 'react-router-dom' 
import './Navbar.css';


function Navbar() {

  const [click, setClick] = useState(false);
  
  const handleClick = () => setClick(!click);
      
  const closeMenu = () => setClick(false);

  const Signout = (e) => {
    e.preventDefault() ;
    localStorage.clear();
    window.location.href = '/login';
  }
  
  return (
      <nav className=''>
        <div className='navbar'>
            {/* <div className='menu-icon' onClick={handleClick}>
                <i className={click ? 'fa-solid fa-xmark' : 'fa-solid fa-bars'}></i>
            </div> */}
            <NavLink to="/" className="navbar-logo">
                Ascend Travel <i className="fa-brands fa-typo3"></i>
            </NavLink>
            <div className='navlink'>
                <NavLink to ='/home' activeClassName = 'active' className='nav-links' onClick={closeMenu}>
                        Home
                </NavLink>
                <NavLink to ='/userlist' className='nav-links' onClick={closeMenu}>
                    UserList
                </NavLink>
                <NavLink to ='/create' className='nav-links' onClick={closeMenu}>
                    CreateUser
                </NavLink>
                <NavLink to ='/edit' className='nav-links' onClick={closeMenu}>
                    CreateUser
                </NavLink>
                <form onSubmit={Signout} className = "signoutButton" >
                    <button type = "submit" >SIGN OUT</button>
                </form>
            </div>
        </div>
    </nav>

  )
}

export default Navbar