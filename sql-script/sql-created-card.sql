CREATE TABLE Card (
    CardID int PRIMARY KEY Identity,
	CardName NVARCHAR(100),
    CardDetails NVARCHAR(100),
    Status NVARCHAR(50),
	CreateAt DATETIME DEFAULT CURRENT_TIMESTAMP,
	UpdateAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CreateByUserID int 
);

CREATE TABLE User_Card (
	UserID int,
	CardID int,
	CONSTRAINT comp_key PRIMARY KEY(UserID,CardID),
	CONSTRAINT card_foreign_key FOREIGN KEY(CardID) REFERENCES Card(CardID)
);

SELECT ... FROM [table] GROUP BY ... HAVING ...


-- INSERT INTO [Card] (CardDetails, Status) VALUES ('Something todo','todo')
-- INSERT INTO [Card] (CardDetails, Status) VALUES ('Doing','doing')
-- INSERT INTO [Card] (CardDetails, Status) VALUES ('Finish work','finish')
