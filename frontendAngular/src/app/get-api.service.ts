import { Injectable } from '@angular/core';
import { HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class GetApiService {


  constructor(
    private http:HttpClient
  ) { }

  apiCall(){
    
  }

  apiGetUserDetails(userID: string){
    const url = `http://localhost:8080/user/api/user/${userID}/details` ;
    return this.http.get(url);
  }

  apiGetUserList() {
    const url = "http://localhost:8080/user/api/user/list?search=&roleID=&page=1&pageSize=10";
    return this.http.get(url);
  }
}
