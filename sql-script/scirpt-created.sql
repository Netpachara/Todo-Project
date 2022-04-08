CREATE TABLE [user].dbo.[User] (
    UserID int PRIMARY KEY Identity,
    FullName NVARCHAR(100) NOT NULL,
    Email NVARCHAR(100) NOT NULL,
    Password NVARCHAR(100) NOT NULL
);

CREATE TABLE Role (
	RoleID int PRIMARY KEY Identity,
	Title NVARCHAR(50) NOT NULL
);

CREATE TABLE UserRole (
	UserID int,
	RoleID int,
	CONSTRAINT comp_key PRIMARY KEY(UserID,RoleID),
	CONSTRAINT user_foreign_key FOREIGN KEY(UserID) REFERENCES [User](UserID),
	CONSTRAINT role_foreign_key FOREIGN KEY(RoleID) REFERENCES Role(RoleID)
);
