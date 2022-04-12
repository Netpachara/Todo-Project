use [user]
CREATE TABLE [User] (
    UserID int PRIMARY KEY Identity,
    FullName NVARCHAR(100) NOT NULL,
    Email NVARCHAR(100) NOT NULL,
    Password NVARCHAR(100) NOT NULL
);

CREATE TABLE Role (
	RoleID int PRIMARY KEY Identity,
	Title NVARCHAR(50) NOT NULL
);

CREATE TABLE User_Role (
	UserID int,
	RoleID int,
	CONSTRAINT comp_key PRIMARY KEY(UserID,RoleID),
	CONSTRAINT user_foreign_key FOREIGN KEY(UserID) REFERENCES [User](UserID),
	CONSTRAINT role_foreign_key FOREIGN KEY(RoleID) REFERENCES Role(RoleID)
);

INSERT INTO [Role](Title) VALUES('admin')
INSERT INTO [Role](Title) VALUES('member')
