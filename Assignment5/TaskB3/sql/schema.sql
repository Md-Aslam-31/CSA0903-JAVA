CREATE TABLE bus_pass (
    pass_id VARCHAR(30) PRIMARY KEY,
    reg_no VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    route_no INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    CONSTRAINT uq_bus_pass_reg_route UNIQUE (reg_no, route_no)
);
CREATE INDEX ix_bus_pass_route ON bus_pass(route_no);
