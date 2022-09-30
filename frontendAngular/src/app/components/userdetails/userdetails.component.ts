import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { GetApiService } from 'src/app/get-api.service';

@Component({
  selector: 'app-userdetails',
  templateUrl: './userdetails.component.html',
  styleUrls: ['./userdetails.component.scss']
})
export class UserdetailsComponent implements OnInit {

  private routeSub: Subscription;

  id = 0;
  fullName = "";
  email = "";
  responseRoleList = [];

  constructor(private api: GetApiService, private route: ActivatedRoute) { }

  ngOnInit(){
    this.routeSub = this.route.params.subscribe((params) => {
      console.log(params);
      this.api.apiGetUserDetails(params['id']).subscribe((data) => {
        console.warn(data);
        this.id = data['userID'];
        this.fullName = data['fullName'];
        this.email = data['email'];
        this.responseRoleList = data['responseRoleList'];
      })
    })
    
  }

}
